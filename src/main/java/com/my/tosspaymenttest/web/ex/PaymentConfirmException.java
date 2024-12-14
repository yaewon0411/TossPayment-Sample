package com.my.tosspaymenttest.web.ex;

import lombok.Getter;

@Getter
public class PaymentConfirmException extends RuntimeException{

    private final String message;

    public PaymentConfirmException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}
