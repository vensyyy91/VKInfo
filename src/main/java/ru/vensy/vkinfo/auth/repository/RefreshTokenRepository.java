package ru.vensy.vkinfo.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vensy.vkinfo.auth.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
}