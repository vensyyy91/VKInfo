package ru.vensy.vkinfo.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.vensy.vkinfo.api.dto.LoginRequest;
import ru.vensy.vkinfo.api.dto.UserCookies;

public interface AuthService {
    UserCookies login(LoginRequest request);

    UserCookies logout(HttpServletRequest request);

    UserCookies refresh(HttpServletRequest request);
}