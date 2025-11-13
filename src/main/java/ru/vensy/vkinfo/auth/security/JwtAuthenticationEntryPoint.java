package ru.vensy.vkinfo.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ru.vensy.vkinfo.api.dto.AppError;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper;
    private final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    public JwtAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.info("Authentification error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        AppError error = AppError.builder()
                .status(401)
                .reason("Unauthorized")
                .message(authException.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        mapper.writeValue(response.getOutputStream(), error);
    }
}