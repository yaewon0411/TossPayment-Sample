package com.my.tosspaymenttest.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class RestClientConfig {

    //토스페이먼츠에서 제공하는 테스트키
    @Value("${toss.widget-secret-key}")
    private String WIDGET_SECRET_KEY;

    private static final String BASIC = "Basic ";

    @Bean
    public RestClient restClient(){
        return RestClient.builder()
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader("Authorization", getAuthorization())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    private String getAuthorization(){
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodeBytes = encoder.encode((WIDGET_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));
        return BASIC + new String(encodeBytes);
    }
}
