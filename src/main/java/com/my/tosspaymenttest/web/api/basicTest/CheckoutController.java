package com.my.tosspaymenttest.web.api.basicTest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CheckoutController {

    @GetMapping("/checkout")
    public ModelAndView getCheckoutPage(){
        return new ModelAndView("redirect:/basicTest/checkout.html");
    }
}
