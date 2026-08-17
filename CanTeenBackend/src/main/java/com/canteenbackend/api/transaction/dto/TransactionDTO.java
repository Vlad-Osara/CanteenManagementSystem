package com.canteenbackend.api.transaction.dto;

import com.canteenbackend.api.transaction.model.TransactionType;
import com.canteenbackend.helper.base.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class TransactionDTO extends BaseDTO {
    private UUID customerId; // LAZY
    private BigDecimal amount;
    private TransactionType type;
    private String description;
}
