package com.my.tosspaymenttest.web.ex;

import lombok.Getter;

@Getter
public class PaymentValidationException extends RuntimeException{
    private final String message;

    public PaymentValidationException(String message) {
        super(message);
        this.message = message;
    }
}
