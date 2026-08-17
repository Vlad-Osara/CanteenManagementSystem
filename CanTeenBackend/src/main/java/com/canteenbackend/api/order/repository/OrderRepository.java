package com.canteenbackend.api.order.repository;

import com.canteenbackend.api.order.model.Order;
import com.canteenbackend.api.order.model.OrderStatus;
import com.canteenbackend.helper.base.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class OrderRepository extends BaseRepository<Order, UUID, OrderJpaRepository>{
    public OrderRepository(OrderJpaRepository repository) {
        super(repository, Order.class);
    }

    public Page<Order> findByCustomerId(UUID userId, Pageable pageable){
        return repository.findByCustomerId(userId, pageable);
    }

    public Page<Order> findByStatusIn(List<OrderStatus> status, Pageable pageable){
        return repository.findByStatusIn(status, pageable);
    }
}
