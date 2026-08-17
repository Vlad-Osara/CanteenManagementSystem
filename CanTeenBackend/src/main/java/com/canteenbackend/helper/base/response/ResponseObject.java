package com.canteenbackend.helper.base.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseObject<T> {
    private String message;
    private T data;

    public static <T> ResponseObject<T> success(String message, T data) {
        return new ResponseObject<>(message, data);
    }
}
