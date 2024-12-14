package com.my.tosspaymenttest.web.domain.payment;

import com.my.tosspaymenttest.web.domain.BaseEntity;
import com.my.tosspaymenttest.web.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String method; //결제 수단 ( 카드, 가상계좌, 간편결제, 휴대폰, 계좌이체, 문화상품권, 도서문화상품권, 게임문화상품권 중 1)

    private String provider; //결제 수단이 간편결제일 때, 선택한 간편 결제사 코드 (카카오페이, 네이버페이..)

    private Integer amount; //결제 금액

    private String status; //결제 처리 상태

    private LocalDateTime paidAt; //결제 승인이 일어난 시간 날짜 정보

    @Lob
    private String failReason; //실패 사유

    @Column(unique = true, nullable = false)
    private String merchantUid; // 주문번호: 주문 식별자

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    public void updateFailInfo(String failReason, String status){
        this.failReason = failReason;
        this.status = status;
    }

    @Builder
    public Payment(Long id, User user, String method, String provider, Integer amount, String status, LocalDateTime paidAt, String failReason, String merchantUid, PaymentType paymentType) {
        this.id = id;
        this.user = user;
        this.method = method;
        this.provider = provider;
        this.amount = amount;
        this.status = status;
        this.paidAt = paidAt;
        this.failReason = failReason;
        this.merchantUid = merchantUid;
        this.paymentType = paymentType;
    }
}
