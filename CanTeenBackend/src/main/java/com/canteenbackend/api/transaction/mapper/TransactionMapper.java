package com.canteenbackend.api.transaction.mapper;

import com.canteenbackend.api.transaction.dto.TransactionDTO;
import com.canteenbackend.api.transaction.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public TransactionDTO toTransactionDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .customerId(transaction.getCustomer().getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
