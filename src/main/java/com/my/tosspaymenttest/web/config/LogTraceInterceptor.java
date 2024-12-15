package com.my.tosspaymenttest.web.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@Slf4j
public class LogTraceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        MDC.put("requestId", requestId);
        MDC.put("method", method);
        MDC.put("uri", requestUri);

        log.info("요청 접수: {} {}", method, requestUri);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("요청 완료: {} -> status {}", request.getRequestURI(), response.getStatus());
        MDC.clear();
    }
}
