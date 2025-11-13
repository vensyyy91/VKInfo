package ru.vensy.vkinfo.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.vensy.vkinfo.api.dto.LoginRequest;
import ru.vensy.vkinfo.api.dto.UserCookies;
import ru.vensy.vkinfo.api.dto.UserInfo;
import ru.vensy.vkinfo.auth.entity.RefreshToken;
import ru.vensy.vkinfo.auth.entity.User;
import ru.vensy.vkinfo.auth.security.JwtUtil;
import ru.vensy.vkinfo.auth.security.UserDetailsImpl;
import ru.vensy.vkinfo.auth.service.RefreshTokenService;
import ru.vensy.vkinfo.auth.service.UserService;
import ru.vensy.vkinfo.auth.service.impl.AuthServiceImpl;
import ru.vensy.vkinfo.exception.AuthException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserService userService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private HttpServletRequest request;
    @InjectMocks
    private AuthServiceImpl authService;
    private User user;
    private UserInfo userInfo;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("admin");

        userInfo = new UserInfo(user.getId(), user.getUsername());

        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));
    }

    @Test
    void login_shouldReturnUserCookies_whenCredentialsValid() {
        LoginRequest request = new LoginRequest("admin", "qwerty");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);
        when(userService.getUserInfoByUsername("admin"))
                .thenReturn(userInfo);
        when(jwtUtil.generateAccessJwtCookie("admin"))
                .thenReturn(ResponseCookie.from("access", "access-token").build());
        when(jwtUtil.generateRefreshJwtCookie("refresh-token"))
                .thenReturn(ResponseCookie.from("refresh", "refresh-token").build());
        when(refreshTokenService.create(1L))
                .thenReturn(refreshToken);

        UserCookies cookies = authService.login(request);

        assertEquals("admin", cookies.user().username());
        assertTrue(cookies.accessCookie().contains("access-token"));
        assertTrue(cookies.refreshCookie().contains("refresh-token"));

        verify(authenticationManager).authenticate(any());
        verify(refreshTokenService).create(1L);
    }

    @Test
    void logout_shouldDeleteRefreshTokenAndReturnCleanCookies_whenUserAuthenticated() {
        UserDetailsImpl details = mock(UserDetailsImpl.class);
        when(details.getUser())
                .thenReturn(user);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal())
                .thenReturn(details);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(jwtUtil.getJwtRefreshFromCookies(request))
                .thenReturn("refresh-token");
        when(refreshTokenService.findByToken("refresh-token"))
                .thenReturn(refreshToken);
        when(jwtUtil.getCleanJwtCookie())
                .thenReturn(ResponseCookie.from("access", "").build());
        when(jwtUtil.getCleanJwtRefreshCookie())
                .thenReturn(ResponseCookie.from("refresh", "").build());

        UserCookies result = authService.logout(request);

        assertEquals(user.getUsername(), result.user().username());
        assertEquals("access=", result.accessCookie());
        assertEquals("refresh=", result.refreshCookie());

        verify(refreshTokenService)
                .deleteByToken(refreshToken);
    }

    @Test
    void logout_shouldThrowAuthException_whenUserNotAuthenticated() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthException.class, () -> authService.logout(request));
    }

    @Test
    void refresh_shouldGenerateNewAccessCookie_whenValidToken() {
        when(jwtUtil.getJwtRefreshFromCookies(request))
                .thenReturn("refresh-token");
        when(refreshTokenService.findByToken("refresh-token"))
                .thenReturn(refreshToken);
        when(refreshTokenService.verifyExpiration(refreshToken))
                .thenReturn(refreshToken);
        when(jwtUtil.generateAccessJwtCookie("admin"))
                .thenReturn(ResponseCookie.from("access", "new-access-token").build());

        UserCookies result = authService.refresh(request);

        assertEquals("admin", result.user().username());
        assertTrue(result.accessCookie().contains("new-access-token"));
    }

    @Test
    void refresh_shouldThrowAuthException_whenNoToken() {
        when(jwtUtil.getJwtRefreshFromCookies(request))
                .thenReturn(null);

        AuthException ex = assertThrows(AuthException.class, () -> authService.refresh(request));
        assertTrue(ex.getMessage().contains("Необходим refresh-токен"));
    }

    @Test
    void refresh_shouldThrowAuthException_whenTokenExpired() {
        when(jwtUtil.getJwtRefreshFromCookies(request))
                .thenReturn("refresh-token");
        when(refreshTokenService.findByToken("refresh-token"))
                .thenReturn(refreshToken);
        when(refreshTokenService.verifyExpiration(refreshToken))
                .thenThrow(new AuthException("Истек срок действия refresh-токена. Пожалуйста, авторизуйтесь заново."));

        AuthException ex = assertThrows(AuthException.class, () -> authService.refresh(request));
        assertTrue(ex.getMessage().contains("Истек срок действия refresh-токена"));
    }
}