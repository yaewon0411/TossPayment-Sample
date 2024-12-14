package com.my.tosspaymenttest.web.ex;

import lombok.Getter;

@Getter
public class PointChargeException extends RuntimeException{

    private final String message;

    public PointChargeException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }
}
