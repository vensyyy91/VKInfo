package ru.vensy.vkinfo.auth.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vensy.vkinfo.api.dto.LoginRequest;
import ru.vensy.vkinfo.api.dto.UserCookies;
import ru.vensy.vkinfo.api.dto.UserInfo;
import ru.vensy.vkinfo.auth.entity.RefreshToken;
import ru.vensy.vkinfo.auth.entity.User;
import ru.vensy.vkinfo.auth.security.JwtUtil;
import ru.vensy.vkinfo.auth.security.UserDetailsImpl;
import ru.vensy.vkinfo.auth.service.AuthService;
import ru.vensy.vkinfo.auth.service.RefreshTokenService;
import ru.vensy.vkinfo.auth.service.UserService;
import ru.vensy.vkinfo.exception.AuthException;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                           UserService userService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    @Transactional
    public UserCookies login(LoginRequest request) {
        String username = request.username();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserInfo user = userService.getUserInfoByUsername(username);

        ResponseCookie jwtAccessCookie = jwtUtil.generateAccessJwtCookie(username);
        RefreshToken refreshToken = refreshTokenService.create(user.id());
        ResponseCookie jwtRefreshCookie = jwtUtil.generateRefreshJwtCookie(refreshToken.getToken());

        UserCookies response = new UserCookies(
                user,
                jwtAccessCookie.toString(),
                jwtRefreshCookie.toString()
        );

        log.info("User {} logged in successfully", username);

        return response;
    }

    @Override
    @Transactional
    public UserCookies logout(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl details) {
            User user = details.getUser();

            String token = jwtUtil.getJwtRefreshFromCookies(request);
            if (token != null && !token.isBlank()) {
                RefreshToken refreshToken = refreshTokenService.findByToken(token);
                refreshTokenService.deleteByToken(refreshToken);
            }

            SecurityContext context = SecurityContextHolder.getContext();
            SecurityContextHolder.clearContext();
            context.setAuthentication(null);

            ResponseCookie access = jwtUtil.getCleanJwtCookie();
            ResponseCookie refresh = jwtUtil.getCleanJwtRefreshCookie();

            UserCookies response = new UserCookies(
                    new UserInfo(user.getId(), user.getUsername()),
                    access.toString(),
                    refresh.toString()
            );

            log.info("User {} logged out successfully", user.getUsername());

            return response;
        }

        throw new AuthException("Пользователь не аутентифицирован.");
    }

    @Override
    @Transactional
    public UserCookies refresh(HttpServletRequest request) {
        String token = jwtUtil.getJwtRefreshFromCookies(request);
        if (token == null || token.isBlank()) {
            throw new AuthException("Необходим refresh-токен");
        }

        RefreshToken refreshToken = refreshTokenService.verifyExpiration(refreshTokenService.findByToken(token));
        User user = refreshToken.getUser();
        ResponseCookie access = jwtUtil.generateAccessJwtCookie(user.getUsername());

        UserCookies response = new UserCookies(
                new UserInfo(user.getId(), user.getUsername()),
                access.toString(),
                token
        );

        log.info("Access token successfully refreshed.");

        return response;
    }
}