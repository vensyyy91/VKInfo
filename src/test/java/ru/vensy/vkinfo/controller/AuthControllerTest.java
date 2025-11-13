package ru.vensy.vkinfo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.vensy.vkinfo.config.TestSecurityConfig;
import ru.vensy.vkinfo.api.controller.AuthController;
import ru.vensy.vkinfo.api.dto.LoginRequest;
import ru.vensy.vkinfo.api.dto.UserCookies;
import ru.vensy.vkinfo.api.dto.UserInfo;
import ru.vensy.vkinfo.auth.security.AuthTokenFilter;
import ru.vensy.vkinfo.auth.service.AuthService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = ru.vensy.vkinfo.auth.security.SecurityConfig.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = AuthTokenFilter.class
                )
        }
)
@Import(TestSecurityConfig.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private AuthService authService;
    private LoginRequest request;
    private UserInfo user;

    @BeforeEach
    void setUp() {
        request = new LoginRequest("admin", "qwerty123");
        user = new UserInfo(1L, "admin");
    }

    @Test
    void login_shouldReturnUserAndCookies_whenRequestIsValid() throws Exception {
        UserCookies cookies = new UserCookies(
                user,
                "vk-info-access=value1; Path=/",
                "vk-info-refresh=value2; Path=/"
        );

        when(authService.login(request))
                .thenReturn(cookies);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Set-Cookie", cookies.accessCookie(), cookies.refreshCookie()))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("admin"));

        verify(authService).login(eq(request));
    }

    @Test
    void login_shouldFailValidation_whenRequestIsInvalid() throws Exception {
        LoginRequest invalid = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_shouldReturnSuccessAndCookies() throws Exception {
        UserCookies cookies = new UserCookies(user, "accessCookie=", "refreshCookie=");

        when(authService.logout(any(HttpServletRequest.class)))
                .thenReturn(cookies);

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Set-Cookie", cookies.accessCookie(), cookies.refreshCookie()))
                .andExpect(content().string("Выход выполнен успешно."));

        verify(authService).logout(any(HttpServletRequest.class));
    }

    @Test
    void refresh_shouldReturnUserAndAccessCookie() throws Exception {
        UserCookies cookies = new UserCookies(user, "accessCookie=newToken", "refreshCookie=ignored");

        when(authService.refresh(any(HttpServletRequest.class)))
                .thenReturn(cookies);

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", cookies.accessCookie()))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("admin"));

        verify(authService).refresh(any(HttpServletRequest.class));
    }
}