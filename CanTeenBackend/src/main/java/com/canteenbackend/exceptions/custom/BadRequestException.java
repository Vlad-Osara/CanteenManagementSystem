package com.canteenbackend.exceptions.custom;

import lombok.Getter;

import java.util.Map;

@Getter
public class BadRequestException extends RuntimeException {
    private final Map<String, String> data;

    // Constructor 1: Chỉ truyền message (data sẽ là null)
    public BadRequestException(String message) {
        super(message);
        this.data = null;
    }

    // Constructor 2: Truyền cả message và data kèm theo
    public BadRequestException(String message, Map<String, String> data) {
        super(message);
        this.data = data;
    }
}
