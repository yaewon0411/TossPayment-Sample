package com.my.tosspaymenttest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.tosspaymenttest.client.dto.TossPaymentCancelReqDto;
import com.my.tosspaymenttest.client.dto.TossPaymentErrorRespDto;
import com.my.tosspaymenttest.client.ex.PaymentFeature;
import com.my.tosspaymenttest.client.ex.TossPaymentErrorCode;
import com.my.tosspaymenttest.client.ex.TossPaymentException;
import com.my.tosspaymenttest.client.dto.TossPaymentReqDto;
import com.my.tosspaymenttest.client.dto.TossPaymentRespDto;
import com.my.tosspaymenttest.client.ex.TossPaymentSystemException;
import com.my.tosspaymenttest.client.ex.badRequest.TossPaymentBadRequestException;
import com.my.tosspaymenttest.client.ex.forbidden.TossPaymentForbiddenException;
import com.my.tosspaymenttest.client.ex.notFound.TossPaymentNotFoundException;
import com.my.tosspaymenttest.client.ex.serverError.TossPaymentServerException;
import com.my.tosspaymenttest.client.ex.unAuthorized.TossPaymentUnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@Service
@Slf4j
public class TossPaymentClient {
    private final RestClient restClient;
    private final ObjectMapper om;
    private static final String ERROR_LOG_FORMAT = "[토스페이먼츠 %s 실패] paymentKey={}, message={}";
    private static final String SYSTEM_ERROR_LOG_FORMAT = "[토스페이먼츠 %s 중 예상치 못한 예외 발생] paymentKey={}";

    public TossPaymentRespDto cancelPayment(String paymentKey, TossPaymentCancelReqDto cancelReqDto){
        try {
            TossPaymentRespDto response = restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .body(cancelReqDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ((request, errorResponse) -> {
                        TossPaymentErrorRespDto tossPaymentErrorRespDto = bodyTo(errorResponse);
                        TossPaymentErrorCode errorCode = tossPaymentErrorRespDto.getErrorCode();
                        handleError(paymentKey, errorResponse, errorCode);
                    }))
                    .body(TossPaymentRespDto.class);

            String cancelStatus = response.getCancels().stream()
                    .findFirst()
                    .map(TossPaymentRespDto.Cancels::getCancelStatus)
                    .orElse("UNKNOWN");

            log.info("check 1: [토스페이먼츠 결제 취소 성공] paymentKey={}, orderId={}, totalAmount={}, status = {} ",
                    response.getPaymentKey(),
                    response.getOrderId(),
                    response.getTotalAmount(),
                    cancelStatus
            );
            return response;
        } catch (Exception e){
            handlePaymentException(PaymentFeature.CANCEL, paymentKey, e);
            throw e; //반환값이 있기 때문에 충족시키기 위해 명시적인 throw 추가
        }
    }

    public TossPaymentRespDto checkPaymentInfo(String paymentKey){
        try {
            TossPaymentRespDto response = restClient.get()
                    .uri("/v1/payments/{paymentKey}", paymentKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ((request, errorResponse) -> {
                        TossPaymentErrorRespDto tossPaymentErrorRespDto = bodyTo(errorResponse);
                        TossPaymentErrorCode errorCode = tossPaymentErrorRespDto.getErrorCode();
                        handleError(paymentKey, errorResponse, errorCode);
                    }))
                    .body(TossPaymentRespDto.class);
            log.info("[토스페이먼츠 결제 정보 조회 성공] paymentKey={}, orderId={}, totalAmount={}",
                    response.getPaymentKey(),
                    response.getOrderId(),
                    response.getTotalAmount()
            );
            return response;
        } catch (Exception e) {
            handlePaymentException(PaymentFeature.INQUIRY, paymentKey, e);
            throw e;
        }
    }


    public TossPaymentRespDto confirmPayment(TossPaymentReqDto tossPaymentReqDto){
        log.info("[토스페이먼츠 결제 승인 요청] paymentKey={}, orderId={}, amount={}",
                tossPaymentReqDto.getPaymentKey(),
                tossPaymentReqDto.getOrderId(),
                tossPaymentReqDto.getAmount()
        );
        try {
            TossPaymentRespDto response = restClient
                    .post()
                    .uri("/v1/payments/confirm")
                    .body(tossPaymentReqDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ((request, errorResponse) -> {
                        TossPaymentErrorRespDto tossPaymentErrorRespDto = bodyTo(errorResponse);
                        TossPaymentErrorCode errorCode = tossPaymentErrorRespDto.getErrorCode();
                        String paymentKey = tossPaymentErrorRespDto.getPaymentKey();
                        handleError(paymentKey, errorResponse, errorCode);
                    }))
                    .body(TossPaymentRespDto.class);

            log.info("[토스페이먼츠 결제 승인 성공] paymentKey={}, orderId={}, totalAmount={}",
                    response.getPaymentKey(),
                    response.getOrderId(),
                    response.getTotalAmount()
            );
            return response;
        } catch (Exception e) {
            handlePaymentException(PaymentFeature.CONFIRM, tossPaymentReqDto.getPaymentKey(), e);
            throw e;
        }
    }


    private void handlePaymentException(PaymentFeature feature, String paymentKey, Exception e) {
        if (e instanceof TossPaymentException) {
            log.warn(ERROR_LOG_FORMAT.formatted(feature.getDescription()), paymentKey, ((TossPaymentException) e).getErrorCode().getMessage(), e);
            throw (TossPaymentException) e;
        }
        log.error(SYSTEM_ERROR_LOG_FORMAT.formatted(feature.getDescription()), paymentKey, e);
        throw new TossPaymentSystemException(feature.getDescription() + " 중 예상치 못한 오류가 발생했습니다", e);
    }

    private void handleError(String paymentKey, ClientHttpResponse errorResponse, TossPaymentErrorCode errorCode) throws IOException {
        switch(errorResponse.getStatusCode().value()){
            case 400 -> throw new TossPaymentBadRequestException(errorCode, paymentKey);
            case 401 -> throw new TossPaymentUnauthorizedException(errorCode, paymentKey);
            case 403 -> throw new TossPaymentForbiddenException(errorCode, paymentKey);
            case 404 -> throw new TossPaymentNotFoundException(errorCode, paymentKey);
            case 500 -> throw new TossPaymentServerException(errorCode, paymentKey);
            default -> throw new TossPaymentServerException(
                    TossPaymentErrorCode.UNKNOWN_ERROR,
                    paymentKey
            );
        }
    }

    private TossPaymentErrorRespDto bodyTo(ClientHttpResponse response) throws IOException {
        InputStream body = response.getBody();
        return om.readValue(body, TossPaymentErrorRespDto.class);
    }

}
