package com.canteenbackend.helper.base.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public abstract class BaseDTO implements Serializable {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
