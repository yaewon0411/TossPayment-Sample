package com.my.tosspaymenttest.web.api.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PaymentViewController {
    @Value("${toss.client-key}")
    private String clientKey;

    @GetMapping("/point/{userId}")
    public String getPointCharge(@PathVariable("userId") Long userId, Model model) {
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("userId", userId);
        return "point-charge";
    }

    @GetMapping("/point-charge-success")
    public String getPointChargeSuccess(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId") String orderId,
            @RequestParam("amount") String amount,
            Model model
    ) {
        model.addAttribute("paymentKey", paymentKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        return "point-charge-success";
    }
}
