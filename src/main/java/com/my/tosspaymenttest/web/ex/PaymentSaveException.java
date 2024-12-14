package com.my.tosspaymenttest.web.ex;

import lombok.Getter;

@Getter
public class PaymentSaveException extends RuntimeException{
    private final String message;

    public PaymentSaveException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }
}
