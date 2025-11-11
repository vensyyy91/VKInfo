package ru.vensy.vkinfo.auth.service;

import ru.vensy.vkinfo.auth.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken create(Long userId);

    RefreshToken findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken refreshToken);

    void deleteByToken(RefreshToken token);
}