package com.example.transferservice.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseData<T> {
    private final T data;

    // 편의 메서드
    public static <T> ResponseData<T> of(T data) {
        return new ResponseData<>(data);
    }
}