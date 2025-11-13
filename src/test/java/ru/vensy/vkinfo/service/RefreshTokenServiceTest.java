package ru.vensy.vkinfo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vensy.vkinfo.auth.entity.RefreshToken;
import ru.vensy.vkinfo.auth.entity.User;
import ru.vensy.vkinfo.auth.repository.RefreshTokenRepository;
import ru.vensy.vkinfo.auth.repository.UserRepository;
import ru.vensy.vkinfo.auth.service.RefreshTokenService;
import ru.vensy.vkinfo.auth.service.impl.RefreshTokenServiceImpl;
import ru.vensy.vkinfo.exception.AuthException;
import ru.vensy.vkinfo.exception.NotFoundException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    private RefreshTokenService refreshTokenService;
    private User user;
    private RefreshToken token;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenServiceImpl(
                3600000L,
                refreshTokenRepository,
                userRepository
        );

        user = new User();
        user.setId(1L);
        user.setUsername("admin");

        token = new RefreshToken();
        token.setId(1L);
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plusSeconds(3600));
    }

    @Test
    void create_shouldCreateToken_whenUserExists() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(token);

        RefreshToken result = refreshTokenService.create(1L);

        assertNotNull(result);
        assertEquals(token, result);
        assertEquals(user, result.getUser());

        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void create_shouldThrowNotFound_whenUserNotExists() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class, () -> refreshTokenService.create(99L));

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void findByToken_shouldReturnToken_whenExists() {
        when(refreshTokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.findByToken(token.getToken());

        assertEquals(token, result);

        verify(refreshTokenRepository).findByToken(token.getToken());
    }

    @Test
    void findByToken_shouldThrowNotFound_whenNotExists() {
        when(refreshTokenRepository.findByToken("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> refreshTokenService.findByToken("unknown")
        );

        verify(refreshTokenRepository).findByToken("unknown");
    }

    @Test
    void verifyExpiration_shouldReturnSameToken_whenValid() {
        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertEquals(token, result);

        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_shouldThrowAuthException_whenExpired() {
        token.setExpiresAt(Instant.now().minusSeconds(10));

        assertThrows(AuthException.class, () -> refreshTokenService.verifyExpiration(token));

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void deleteByToken_shouldDeleteToken() {
        refreshTokenService.deleteByToken(token);

        verify(refreshTokenRepository).delete(token);
    }
}