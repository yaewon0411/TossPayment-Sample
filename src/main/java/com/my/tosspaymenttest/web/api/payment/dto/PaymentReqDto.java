package com.my.tosspaymenttest.web.api.payment.dto;

import com.my.tosspaymenttest.client.dto.TossPaymentRespDto;
import com.my.tosspaymenttest.web.domain.payment.Payment;
import com.my.tosspaymenttest.web.domain.payment.PaymentType;
import com.my.tosspaymenttest.web.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Getter
@ToString
public class PaymentReqDto {


    private String orderId;
    private Integer amount;
    private String paymentKey;
    private Long userId;

    public Payment toEntity(TossPaymentRespDto result, User user, PaymentType paymentType){
        return Payment.builder()
                .method(result.getMethod())
                .user(user)
                .paymentType(paymentType)
                .amount(result.getTotalAmount())
                .status(result.getStatus())
                .paidAt(LocalDateTime.parse(result.getApprovedAt(), DateTimeFormatter.ISO_DATE_TIME))
                .failReason(result.getFailure() != null? result.getFailure().getMessage() : null)
                .merchantUid(result.getOrderId())
                .provider(result.getEasyPay() != null ? result.getEasyPay().getProvider() : null)
                .build();
    }
}
