package ru.vensy.vkinfo.auth.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vensy.vkinfo.auth.entity.RefreshToken;
import ru.vensy.vkinfo.auth.entity.User;
import ru.vensy.vkinfo.auth.repository.RefreshTokenRepository;
import ru.vensy.vkinfo.auth.repository.UserRepository;
import ru.vensy.vkinfo.auth.service.RefreshTokenService;
import ru.vensy.vkinfo.exception.AuthException;
import ru.vensy.vkinfo.exception.NotFoundException;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Value("${security.jwt.refresh-cookie.expiration-ms}")
    private Long refreshTokenDuration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public RefreshToken create(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenDuration));

        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);

        log.info("Saved refresh token with id={} for user with id={}",  savedRefreshToken.getId(), userId);

        return savedRefreshToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Refresh-токен не найден."));

        log.info("Found refresh token with id={}", refreshToken.getId());

        return refreshToken;
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiresAt().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthException("Истек срок действия refresh-токена. Пожалуйста, авторизуйтесь заново.");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void deleteByToken(RefreshToken token) {
        refreshTokenRepository.delete(token);
    }
}