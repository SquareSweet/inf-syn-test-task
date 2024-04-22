package utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import model.User;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;

@Slf4j
public class JwtTokenUtils {
    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;
    private final Duration jwtAccessLifetime;
    private final Duration jwtRefreshLifetime;

    public JwtTokenUtils() {
        Properties jwtProperties = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/jwt.properties")) {
            jwtProperties.load(in);
        } catch (IOException e) {
            log.error("Error occurred while reading database properties file: " + e.getMessage());
        }

        jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getProperty("jwt.secret.access")));
        jwtRefreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getProperty("jwt.secret.refresh")));
        jwtAccessLifetime = Duration.parse(jwtProperties.getProperty("jwt.lifetime.access"));
        jwtRefreshLifetime = Duration.parse(jwtProperties.getProperty("jwt.lifetime.refresh"));
    }


    public String generateAccessToken(User user) {
        //Map<String, Object> claims = new HashMap<>();
        //TODO: есть ли смысл добавлять одну роль?
        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + jwtAccessLifetime.toMillis());

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(issuedDate)
                .expiration(expiredDate)
                .signWith(jwtAccessSecret)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + jwtRefreshLifetime.toMillis());

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(issuedDate)
                .expiration(expiredDate)
                .signWith(jwtRefreshSecret)
                .compact();
    }

    public boolean validateAccessToken(String accessToken) {
        return validateToken(accessToken, jwtAccessSecret);
    }

    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, jwtRefreshSecret);
    }

    private boolean validateToken(String token, SecretKey secret) {
        try {
            Jwts.parser().verifyWith(secret).build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported jwt: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Malformed jwt: {}", e.getMessage());
        } catch (SecurityException e) {
            log.error("Invalid signature: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Invalid token: {}", e.getMessage());
        }
        return false;
    }

    public String getUsername(String token) {
        return getAccessClaims(token).getSubject();
    }

    private Claims getAccessClaims(String token) {
        return getClaims(token, jwtAccessSecret);
    }

    public Claims getRefreshClaims(String token) {
        return getClaims(token, jwtRefreshSecret);
    }

    private Claims getClaims(String token, SecretKey secret) {
        return Jwts.parser().verifyWith(secret).build()
                .parseSignedClaims(token)
                .getPayload();
    }
}