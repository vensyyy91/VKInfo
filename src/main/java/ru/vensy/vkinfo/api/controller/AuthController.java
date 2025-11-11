package ru.vensy.vkinfo.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vensy.vkinfo.api.dto.LoginRequest;
import ru.vensy.vkinfo.api.dto.UserCookies;
import ru.vensy.vkinfo.api.dto.UserInfo;
import ru.vensy.vkinfo.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserInfo> login(@RequestBody @Valid LoginRequest body) {
        log.info("POST /api/auth/login; body: {}", body);

        UserCookies cookies = authService.login(body);

        return ResponseEntity.ok()
                .header("Set-Cookie", cookies.accessCookie())
                .header("Set-Cookie", cookies.refreshCookie())
                .body(cookies.user());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        log.info("POST /api/auth/logout");

        UserCookies cookies = authService.logout(request);

        return ResponseEntity.ok()
                .header("Set-Cookie", cookies.accessCookie())
                .header("Set-Cookie", cookies.refreshCookie())
                .body("Выход выполнен успешно.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserInfo> refresh(HttpServletRequest request) {
        log.info("POST /api/auth/refresh");

        UserCookies cookies = authService.refresh(request);

        return ResponseEntity.ok()
                .header("Set-Cookie", cookies.accessCookie())
                .body(cookies.user());
    }
}