package com.canteenbackend.api.order.service;

import com.canteenbackend.api.dish.model.Dish;
import com.canteenbackend.api.dish.repository.DishRepository;
import com.canteenbackend.api.order.dto.OrderDTO;
import com.canteenbackend.api.order.mapper.OrderMapper;
import com.canteenbackend.api.order.model.Order;
import com.canteenbackend.api.order.model.OrderItem;
import com.canteenbackend.api.order.model.OrderStatus;
import com.canteenbackend.api.order.repository.OrderRepository;
import com.canteenbackend.api.order.request.OrderGetRequest;
import com.canteenbackend.api.order.request.OrderItemRequest;
import com.canteenbackend.api.order.request.OrderStoreRequest;
import com.canteenbackend.api.order.request.OrderUpdateRequest;
import com.canteenbackend.api.transaction.model.Transaction;
import com.canteenbackend.api.transaction.model.TransactionType;
import com.canteenbackend.api.transaction.repository.TransactionRepository;
import com.canteenbackend.api.user.model.User;
import com.canteenbackend.api.user.repository.UserRepository;
import com.canteenbackend.exceptions.custom.BadRequestException;
import com.canteenbackend.helper.base.construct.RestfullService;
import com.canteenbackend.utils.security.CustomUserDetails;
import com.canteenbackend.utils.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService extends RestfullService<OrderDTO, OrderGetRequest, OrderStoreRequest, OrderUpdateRequest> {
    private final OrderRepository orderRepository;
    private final DishRepository dishRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final OrderMapper orderMapper;
    private final SecurityUtils securityUtils;
    private final OrderNotificationService orderNotificationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<OrderDTO> getAll(OrderGetRequest orderGetRequest) {
        CustomUserDetails customUserDetails = securityUtils.getCurrentUserDetails();
        Pageable pageable = orderGetRequest.toPageable();

        boolean isStaffOrAdmin = customUserDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));

        if (isStaffOrAdmin) {
            return orderRepository.getAll(pageable).map(orderMapper::toOrderDTO);
        } else {
            return orderRepository.findByCustomerId(customUserDetails.getId(), pageable).map(orderMapper::toOrderDTO);
        }
    }

    @Override
    public OrderDTO get(UUID id) {
        Order order = orderRepository.get(id);
        CustomUserDetails customUserDetails = securityUtils.getCurrentUserDetails();

        boolean isStaffOrAdmin = customUserDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
        boolean isOwner = order.getCustomer().getId().equals(customUserDetails.getId());

        if (!isStaffOrAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền xem đơn hàng này!");
        }
        return orderMapper.toOrderDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO store(OrderStoreRequest orderStoreRequest) {
        User currentUser = securityUtils.getCurrentUserEntity();

        if (!passwordEncoder.matches(orderStoreRequest.getConfirmPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không chính xác! Không thể thực hiện đặt món.");
        }

        Order order = Order.builder()
                .customer(currentUser)
                .status(OrderStatus.PENDING)
                .type(orderStoreRequest.getType())
                .note(orderStoreRequest.getNote())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest orderItemRequest : orderStoreRequest.getItems()) {
            Dish dish = dishRepository.get(orderItemRequest.getDishId());

            if (!dish.getIsAvailable()) {
                throw new BadRequestException("Món ăn [" + dish.getName() + "] hiện đã hết hàng!");
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .dish(dish)
                    .quantity(orderItemRequest.getQuantity())
                    .price(dish.getPrice())
                    .build();

            BigDecimal itemTotal = dish.getPrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            orderItems.add(orderItem);
        }

        order.setTotalPrice(totalAmount);
        order.setOrderItems(orderItems);

        // Khấu trừ tiền tài khoản an toàn chống Race Condition
        int updatedRows = userRepository.deductBalance(currentUser.getId(), totalAmount);
        if (updatedRows == 0) {
            throw new BadRequestException("Số dư ví không đủ! Vui lòng nạp thêm tiền.");
        }

        // Đảm bảo ở Entity Order đã đặt CascadeType.ALL để lưu cả list Items xuống cùng lúc
        Order savedOrder = orderRepository.save(order);

        Transaction transaction = Transaction.builder()
                .customer(currentUser)
                .amount(totalAmount)
                .type(TransactionType.PAYMENT)
                .description("Thanh toán đơn hàng #" + savedOrder.getId())
                .build();
        transactionRepository.save(transaction);

        OrderDTO savedOrderDTO = orderMapper.toOrderDTO(savedOrder);
        orderNotificationService.notifyStaffNewOrder(savedOrderDTO);

        return savedOrderDTO;
    }

    @Override
    @Transactional
    public OrderDTO update(UUID id, OrderUpdateRequest orderUpdateRequest) {
        CustomUserDetails customUserDetails = securityUtils.getCurrentUserDetails();

        // 1. Phân quyền
        boolean isStaffOrAdmin = customUserDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
        if (!isStaffOrAdmin) {
            throw new AccessDeniedException("Không có quyền chỉnh sửa trạng thái đơn hàng!");
        }

        Order currentOrder = orderRepository.get(id);
        OrderStatus oldStatus = currentOrder.getStatus();
        OrderStatus newStatus = orderUpdateRequest.getStatus();

        // 2. NGHIỆP VỤ CHẶN: Nếu đơn hàng đã hủy hoặc đã hoàn thành thì không cho phép thay đổi trạng thái nữa
        if (oldStatus == OrderStatus.CANCELLED || oldStatus == OrderStatus.COMPLETED) {
            throw new BadRequestException("Không thể cập nhật trạng thái cho đơn hàng đã đóng hoặc đã hoàn thành!");
        }

        // 3. Logic hoàn tiền khi Hủy đơn (Chỉ được hủy khi đang chờ xử lý - PENDING)
        if (newStatus == OrderStatus.CANCELLED) {
            if (oldStatus != OrderStatus.PENDING) {
                throw new BadRequestException("Đơn hàng đang được xử lý hoặc đang giao, không thể hủy!");
            }

            int updatedRows = userRepository.addBalance(currentOrder.getCustomer().getId(), currentOrder.getTotalPrice());
            if (updatedRows == 0) {
                throw new BadRequestException("Lỗi trong quá trình hoàn tiền.");
            }

            Transaction transaction = Transaction.builder()
                    .customer(currentOrder.getCustomer())
                    .amount(currentOrder.getTotalPrice())
                    .type(TransactionType.REFUND)
                    .description("Hoàn tiền đơn hàng bị hủy #" + currentOrder.getId())
                    .build();
            transactionRepository.save(transaction);
        }

        // 4. Tối ưu: Đè trực tiếp trạng thái lên thực thể đã được chọn và dùng thẳng JPA repo để lưu
        currentOrder.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(currentOrder);
        OrderDTO updatedDTO = orderMapper.toOrderDTO(updatedOrder);

        //  Báo cho Bếp/Staff cập nhật danh sách công việc
        orderNotificationService.notifyStaffOrderUpdated(updatedDTO);

        // Báo thời gian thực cho Khách Hàng sở hữu đơn này:
        if (updatedOrder.getCustomer() != null) {
            orderNotificationService.notifyCustomerOrderUpdated(
                    updatedOrder.getCustomer().getId(),
                    updatedDTO
            );
        }
        // TODO: Bổ sung WEB-SOCKET sau tại đây
        return updatedDTO;
    }

    @Override
    public OrderDTO destroy(UUID id) {
        return null;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<OrderDTO> getActiveOrder(OrderGetRequest orderGetRequest) {
        Pageable pageable = orderGetRequest.toPageable();

        List<OrderStatus> activeStatuses = List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY);

        return orderRepository.findByStatusIn(activeStatuses, pageable)
                .map(orderMapper::toOrderDTO);
    }
}
