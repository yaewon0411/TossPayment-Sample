package com.my.tosspaymenttest.web.api.payment;

import com.my.tosspaymenttest.web.api.payment.dto.PaymentReqDto;
import com.my.tosspaymenttest.web.api.payment.dto.PaymentRespDto;
import com.my.tosspaymenttest.web.service.payment.PaymentService;
import com.my.tosspaymenttest.web.service.payment.PaymentServiceFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceFacade paymentServiceFacade;

    @PostMapping("/payment/confirm")
    public ResponseEntity<PaymentRespDto> confirmPaymentForPointCharge(@RequestBody PaymentReqDto paymentReqDto){
        return ResponseEntity.ok(paymentServiceFacade.processPayment(paymentReqDto));
    }
}
