package com.my.tosspaymenttest.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class TossPaymentRespDto {

    private String version; //객체 응답 버전
    private String paymentKey; //결제 키 값
    private String type; //결제 정보 타입

    private String orderId; //주문번호

    private String orderName; //주문 상품 이름

    private String currency; //결제 시 사용한 통화

    private String method; //결제 수단

    private Integer totalAmount; //결제 금액

    private String status; //결제 처리 상태

    private String requestedAt; //결제가 일어난 시간 날짜 정보

    private String approvedAt; //결제 승인이 일어난 시간 날짜 정보

    private EasyPay easyPay; //간편 결제 정보

    private Checkout checkout; //결제창 정보

    private Receipt receipt; //발행된 영수증 정보

    private Failure failure; //결제 승인에 실패하면 응답으로 받는 에러 객체

    private List<Cancels> cancels; //결제 취소 이력

    @NoArgsConstructor
    @Getter
    public static class Cancels{
        private Integer cancelAmount; //결제 취소 금액
        private String cancelReason; //결제 취소 이유. 최대 길이 200자
        private String transactionKey; //취소 건의 키 값. 여러 건의 취소 거래를 구분하는 데 사용
        private String cancelStatus; //취소 상태. DONE이면 결제가 성공적으로 취소된 상태
        private String canceledAt; //결제 취소가 일어난 날짜와 시간 정보. ISO 8601 형식
    }

    @NoArgsConstructor
    @Getter
    public static class Failure{
        private String code; //오류 타입을 보여주는 에러 코드
        private String message; //에러 발생 이유. 최대 길이 510자
    }

    @NoArgsConstructor
    @Getter
    @ToString
    public static class Receipt{
        private String url; //구매자에게 제공할 수 있는 결제수단별 영수증
    }

    @NoArgsConstructor
    @Getter
    @ToString
    public static class Checkout{
        private String url; //결제창이 열리는 주소
    }

    @NoArgsConstructor
    @Getter
    @ToString
    public static class EasyPay{
        private String provider; //선택한 간편 결제사 코드
        private Integer amount; //간편 결제 서비스에 등록된 계좌 혹은 현금성 포인트로 결제한 금액
        private Integer discountAmount; //간편 결제 서비스의 적립 포인트나 쿠폰 등으로 할인된 금액
    }

}
