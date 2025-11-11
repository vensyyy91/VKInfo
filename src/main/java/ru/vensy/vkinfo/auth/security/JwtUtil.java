package ru.vensy.vkinfo.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${security.jwt.secret}")
    private String jwtSecret;
    @Value("${security.jwt.access-cookie.expiration-ms}")
    private int jwtAccessExpirationMs;
    @Value("${security.jwt.refresh-cookie.expiration-ms}")
    private int jwtRefreshExpirationMs;
    @Value("${security.jwt.access-cookie.name}")
    private String jwtAccessCookie;
    @Value("${security.jwt.refresh-cookie.name}")
    private String jwtRefreshCookie;
    @Value("${security.jwt.access-cookie.path}")
    private String accessTokenPath;
    @Value("${security.jwt.refresh-cookie.path}")
    private String refreshTokenPath;

    private SecretKey secretKey;

    private final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @PostConstruct
    private void init() {
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String getJwtAccessFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request, jwtAccessCookie);
    }

    public String getJwtRefreshFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request, jwtRefreshCookie);
    }

    public ResponseCookie generateAccessJwtCookie(String username) {
        return generateCookie(
                jwtAccessCookie,
                generateJwtToken(username),
                accessTokenPath,
                (long) jwtAccessExpirationMs
        );
    }

    public ResponseCookie getCleanJwtCookie() {
        return generateCookie(jwtAccessCookie, "", accessTokenPath, (long)(1000));
    }

    public ResponseCookie generateRefreshJwtCookie(String refreshToken) {
        return generateCookie(jwtRefreshCookie, refreshToken, refreshTokenPath, (long)jwtRefreshExpirationMs);
    }

    public ResponseCookie getCleanJwtRefreshCookie() {
        return generateCookie(jwtRefreshCookie, "", refreshTokenPath, (long)(1000));
    }

    public String generateJwtToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtAccessExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    private ResponseCookie generateCookie(String name, String value, String path, Long maxAge) {
        return ResponseCookie.from(name, value)
                .path(path)
                .maxAge(maxAge/1000)
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .build();
    }

    private String getCookieValueByName(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }
}