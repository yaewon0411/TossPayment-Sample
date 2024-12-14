package com.my.tosspaymenttest.web.api.payment.dto;

import com.my.tosspaymenttest.client.dto.TossPaymentRespDto;
import com.my.tosspaymenttest.web.domain.pointHistory.PointHistory;
import com.my.tosspaymenttest.web.domain.pointHistory.PointTransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PaymentRespDto{

    private Long userId;
    private String pointTransactionType;

    public PaymentRespDto(PointHistory pointHistory){
        this.userId = pointHistory != null ? pointHistory.getPoint().getUser().getId() : null;
        this.pointTransactionType = pointHistory != null ? pointHistory.getPointTransactionType().toString() : null;
    }
}