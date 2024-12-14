package com.my.tosspaymenttest.client.dto;

import com.my.tosspaymenttest.client.ex.TossPaymentErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class TossPaymentErrorRespDto {
    private String code;
    private String message;
    private String paymentKey;

    public TossPaymentErrorCode getErrorCode() {
        return TossPaymentErrorCode.fromString(code);
    }
}

