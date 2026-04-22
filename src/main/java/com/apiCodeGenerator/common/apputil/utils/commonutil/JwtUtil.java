package com.apiCodeGenerator.common.apputil.utils.commonutil;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-time}")
    private Long jwtExpirationMs;

    private JwtParser jwtParser;
    private SecretKey signingKey;

    // Refresh token validity = 7 days
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000;

    // KEY
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @PostConstruct
    void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtParser = Jwts.parser()
                .verifyWith(signingKey)
                .build();
    }

    // GENERATE ACCESS TOKEN
    public String generateAccessToken(String username, Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("type", "access");
        return buildToken(claims, username, jwtExpirationMs);
    }

    public long getExpirationSeconds() {
        return jwtExpirationMs / 1000;
    }

    public String generateToken(Long userId, UUID userUuid, String email,
                                String userTypeCode) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of(
                        "userUuid",     userUuid.toString(),
                        "email",        email,
                        "userTypeCode", userTypeCode
                ))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // GENERATE REFRESH TOKEN (opaque – stored in DB)
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return buildToken(claims, username, REFRESH_TOKEN_EXPIRATION_MS);
    }

    // BUILD TOKEN
    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    // EXTRACT CLAIMS
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractSessionUuid(String token) {
        String uuid = jwtParser
                .parseSignedClaims(token)
                .getPayload()
                .get("sessionUuid", String.class);

        return uuid != null ? UUID.fromString(uuid) : null;
    }

    public Duration getRemainingValidity(String token) {
        Date expiration = jwtParser
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        long remaining = expiration.getTime() - System.currentTimeMillis();

        return Duration.ofMillis(Math.max(remaining, 0));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // VALIDATE
    public boolean isTokenValid(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username)) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractAllClaims(token).getSubject());
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String extractClaim(String token, String claimKey) {
        return extractAllClaims(token).get(claimKey, String.class);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public long getExpirationTimeMs() {
        return jwtExpirationMs;
    }

    public long getRefreshExpirationTimeMs() {
        return REFRESH_TOKEN_EXPIRATION_MS;
    }
}

