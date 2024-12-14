package com.my.tosspaymenttest.web.ex;

import lombok.Getter;

@Getter
public class CanceledPaymentException extends RuntimeException{

    private final String message;
    public CanceledPaymentException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}
