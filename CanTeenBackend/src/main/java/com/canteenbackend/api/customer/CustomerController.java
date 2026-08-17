package com.canteenbackend.api.customer;

import com.canteenbackend.api.category.request.CategoryGetRequest;
import com.canteenbackend.api.category.service.CategoryService;
import com.canteenbackend.api.customer.service.CustomerService;
import com.canteenbackend.api.dish.request.DishGetRequest;
import com.canteenbackend.api.dish.service.DishService;
import com.canteenbackend.api.order.dto.OrderDTO;
import com.canteenbackend.api.order.request.OrderGetRequest;
import com.canteenbackend.api.order.request.OrderStoreRequest;
import com.canteenbackend.api.order.request.OrderUpdateRequest;
import com.canteenbackend.api.order.service.OrderNotificationService;
import com.canteenbackend.api.order.service.OrderService;
import com.canteenbackend.api.transaction.model.Transaction;
import com.canteenbackend.api.transaction.repository.TransactionRepository;
import com.canteenbackend.api.user.model.User;
import com.canteenbackend.api.user.repository.UserRepository;
import com.canteenbackend.api.user.request.UserUpdateRequest;
import com.canteenbackend.api.user.service.UserService;
import com.canteenbackend.exceptions.custom.BadRequestException;
import com.canteenbackend.helper.base.response.ResponseObject;
import com.canteenbackend.utils.security.SecurityUtils;
import com.canteenbackend.utils.vnpay.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import java.net.URI;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final OrderService orderService;
    private final DishService dishService;
    private final CategoryService categoryService;
    private final CustomerService customerService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final UserService userService;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${canteen.frontend-url:https://huyloi.uk}")
    private String frontendUrl;

    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<?> createPayment(
            @RequestParam BigDecimal amount,
            HttpServletRequest request) {
        try {
            // 1. Lấy thông tin User hiện tại đang đăng nhập hệ thống
            User currentCustomer = securityUtils.getCurrentUserEntity();

            // 2. GỌI PAYMENTSERVICE: Tạo đơn hàng tạm và lấy Link VNPay
            String vnpayUrl = customerService.createVNPayPaymentUrl(currentCustomer, amount, request);

            // 3. Trả link về cho Frontend hiển thị mã QR
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "paymentUrl", vnpayUrl
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "failed",
                    "message", "Không thể tạo link thanh toán: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/vnpay-callback")
    @Transactional
    public ResponseEntity<?> receiveIPN(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        try {
            // 1. Thu thập dữ liệu VNPay gửi về
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = URLDecoder.decode(params.nextElement(), StandardCharsets.US_ASCII);
                String fieldValue = URLDecoder.decode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
                if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            // 2. Sắp xếp dữ liệu để xác minh chữ ký bảo mật
            List<String> fieldNames = new ArrayList<>(fields.keySet());
            Collections.sort(fieldNames);
            StringBuilder signData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                if ((fieldValue != null) && !fieldValue.isEmpty()) {
                    signData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        signData.append('&');
                    }
                }
            }

            // 3. Kiểm tra tính hợp lệ của chữ ký
            String checkSign = VNPayUtil.hmacSHA512(vnp_HashSecret, signData.toString());
            if (!checkSign.equals(vnp_SecureHash)) {
                // Chữ ký không khớp (dữ liệu bị giả mạo)
                return redirectFrontend("/profile?deposit=failed&reason=invalid_checksum");
            }

            // 4. Trích xuất thông tin đơn hàng
            String vnp_TxnRef = fields.get("vnp_TxnRef"); // Đây chính là UUID dạng String
            String vnp_ResponseCode = fields.get("vnp_ResponseCode"); // Mã trạng thái thanh toán (00 = Thành công)

            UUID transactionId = UUID.fromString(vnp_TxnRef);
            Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);

            // Kiểm tra xem mã giao dịch có tồn tại trong hệ thống của bạn không
            if (transactionOpt.isEmpty()) {
                return redirectFrontend("/profile?deposit=failed&reason=order_not_found");
            }

            Transaction transaction = transactionOpt.get();

            // Kiểm tra số tiền nhận được có khớp với số tiền đã tạo đơn hàng không
            long vnp_Amount = Long.parseLong(fields.get("vnp_Amount"));
            long originalAmount = transaction.getAmount().multiply(new java.math.BigDecimal(100)).longValue();
            if (vnp_Amount != originalAmount) {
                return redirectFrontend("/profile?deposit=failed&reason=invalid_amount");
            }

            // Kiểm tra trạng thái đơn hàng (Đảm bảo cơ chế Idempotency - Tránh xử lý trùng)
            if (!transaction.getDescription().contains("Đang xử lý")) {
                return redirectFrontend("/profile?deposit=success&amount=" + transaction.getAmount());
            }

            // 5. CẬP NHẬT TRẠNG THÁI & CỘNG TIỀN VÀO VÍ KHÁCH HÀNG
            if ("00".equals(vnp_ResponseCode)) {
                // Thanh toán THÀNH CÔNG
                User customer = transaction.getCustomer();

                // Thực hiện logic cộng số dư hiện tại của bạn:
                int updatedRows = userRepository.addBalance(customer.getId(), transaction.getAmount());
                if (updatedRows == 0) {
                    throw new BadRequestException("Lỗi trong quá trình nạp tiền.");
                }

                transaction.setDescription("Nạp tiền tự động qua cổng VNPay thành công.");
                transactionRepository.save(transaction);
                return redirectFrontend("/profile?deposit=success&amount=" + transaction.getAmount());
            } else {
                // Thanh toán THẤT BẠI
                transaction.setDescription("Giao dịch nạp tiền qua VNPay thất bại hoặc bị hủy. Mã lỗi: " + vnp_ResponseCode);
                transactionRepository.save(transaction);
                return redirectFrontend("/profile?deposit=failed&code=" + vnp_ResponseCode);
            }

        } catch (Exception e) {
            return redirectFrontend("/profile?deposit=failed&reason=unknown");
        }
    }

    private ResponseEntity<?> redirectFrontend(String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(frontendUrl + path));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/dish")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getDishPaginate(@ModelAttribute DishGetRequest dishGetRequest) {
        return ResponseEntity.ok(ResponseObject.success("Lấy danh sách món ăn thành công", dishService.getAll(dishGetRequest)));
    }

    @GetMapping("/category")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getCategoryPaginate(@ModelAttribute CategoryGetRequest categoryGetRequest) {
        return ResponseEntity.ok(ResponseObject.success("Lấy danh sách danh mục thành công", categoryService.getAll(categoryGetRequest)));
    }

    @GetMapping("/order")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getOrderPaginate(@ModelAttribute OrderGetRequest orderGetRequest) {
        return ResponseEntity.ok(ResponseObject.success("Lấy danh sách đơn món thành công", orderService.getAll(orderGetRequest)));
    }

    @GetMapping("/order/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseObject.success("Lấy đơn thành công", orderService.get(id)));
    }

    @PostMapping("/order")
    public ResponseEntity<?> storeOrder(@RequestBody @Validated OrderStoreRequest orderStoreRequest) {
        OrderDTO createdOrder = orderService.store(orderStoreRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseObject.success("Đặt món thành công", createdOrder));
    }

    //TODO: sửa thành API PUT /api/customer/order/{id}/cancel
//    @PutMapping("/order/{id}")
//    @ResponseStatus(HttpStatus.ACCEPTED)
//    public ResponseEntity<?> updateOrder(@PathVariable UUID id, @Validated @RequestBody OrderUpdateRequest orderUpdateRequest) {
//        return ResponseEntity.ok(ResponseObject.success("Cập nhật trạng thái đơn thành công", orderService.update(id, orderUpdateRequest)));
//    }

    @GetMapping("/transaction")
    @ResponseStatus(HttpStatus.OK) //TODO: bổ sung sau TransactionService
    public ResponseEntity<?> getTransactionPaginate() {
        return ResponseEntity.ok(ResponseObject.success("k", categoryService.getAll(new CategoryGetRequest())));
    }

    @PutMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateProfile(@RequestBody @Validated UserUpdateRequest userUpdateRequest) {
        User currentCustomer = securityUtils.getCurrentUserEntity();
        return ResponseEntity.ok(ResponseObject.success("Cập nhật thông tin cá nhân thành công", userService.update(currentCustomer.getId(), userUpdateRequest)));
    }
}
