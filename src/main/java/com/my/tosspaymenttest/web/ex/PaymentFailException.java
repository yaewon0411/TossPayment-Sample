package com.my.tosspaymenttest.web.ex;

import lombok.Getter;

@Getter
public class PaymentFailException extends RuntimeException{
    private final String message;

    public PaymentFailException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }
}
