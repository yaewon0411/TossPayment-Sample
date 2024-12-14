package com.my.tosspaymenttest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TossPaymentTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TossPaymentTestApplication.class, args);
    }

}
