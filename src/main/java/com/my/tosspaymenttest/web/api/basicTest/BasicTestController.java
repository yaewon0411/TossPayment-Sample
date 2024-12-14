package com.my.tosspaymenttest.web.api.basicTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.tosspaymenttest.client.dto.TossPaymentReqDto;
import com.my.tosspaymenttest.client.dto.TossPaymentRespDto;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

//토스페이먼츠에서 결제 위젯을 연동하여 사용하는 케이스
@RestController
public class BasicTestController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String WIDGET_SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6"; //토스에서 테스트용으로 public 공개함


    @PostMapping(value = "/confirm")
    public ResponseEntity<TossPaymentRespDto> confirmPayment(@RequestBody TossPaymentReqDto tossPaymentReqDto) throws Exception {
        //토스 페이먼츠 요청 데이터 준비
        JSONObject requestBody = createRequestBody(tossPaymentReqDto);
        String authorizations = createAuthHeader();

        //토스 페이먼츠로 결제 승인 API 요청
        HttpURLConnection connection = createConnection(requestBody, authorizations);

        //응답 변환
        JSONObject responseJson = getResponseJson(connection);
        TossPaymentRespDto tossPaymentRespDto = convertToPaymentRespDto(responseJson);

        return ResponseEntity.ok(tossPaymentRespDto);
    }


    private JSONObject createRequestBody(TossPaymentReqDto tossPaymentReqDto){
        JSONObject requestBody = new JSONObject();
        requestBody.put("orderId", tossPaymentReqDto.getOrderId());
        requestBody.put("amount", tossPaymentReqDto.getAmount());
        requestBody.put("paymentKey", tossPaymentReqDto.getPaymentKey());
        return requestBody;
    }

    private String createAuthHeader(){
        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다.
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodeBytes = encoder.encode((WIDGET_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodeBytes);
    }

    private HttpURLConnection createConnection(JSONObject requestBody, String authorizations) throws IOException {
        // 결제를 승인하면 결제수단에서 금액이 차감돼요.
        URL url = new URL(TOSS_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(HttpMethod.POST.name());
        conn.setRequestProperty("Authorization", authorizations);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream outputStream = conn.getOutputStream()) {
            outputStream.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        }

        return conn;
    }

    private JSONObject getResponseJson(HttpURLConnection connection) throws IOException {
        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;

        try(InputStream responseStream = isSuccess? connection.getInputStream() : connection.getErrorStream()){
            Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(reader);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private TossPaymentRespDto convertToPaymentRespDto(JSONObject responseJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = responseJson.toString();
        TossPaymentRespDto tossPaymentRespDto = objectMapper.readValue(jsonString, TossPaymentRespDto.class);

        log.info("결제 정보: {}", objectMapper.writeValueAsString(tossPaymentRespDto));
        return tossPaymentRespDto;
    }




}