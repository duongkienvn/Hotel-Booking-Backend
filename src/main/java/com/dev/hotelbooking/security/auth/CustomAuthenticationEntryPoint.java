package com.dev.hotelbooking.security.auth;

import com.dev.hotelbooking.dto.response.ApiErrorResponse;
import com.dev.hotelbooking.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json");

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(errorCode.getHttpStatus(), errorCode.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(apiErrorResponse));
        response.flushBuffer();
    }
}
