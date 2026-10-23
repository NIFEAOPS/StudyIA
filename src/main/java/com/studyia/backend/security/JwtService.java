package com.studyia.backend.security;

import com.studyia.backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public static final String TOKEN_TYPE_CLAIM = "token_type";
    public static final String ACCESS_TOKEN = "access";
    public static final String REFRESH_TOKEN = "refresh";

    private final SecretKey signingKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(@Value("${studyia.security.jwt-secret}") String secret,
                      @Value("${studyia.security.access-token-expiration}") long accessTokenExpiration,
                      @Value("${studyia.security.refresh-token-expiration}") long refreshTokenExpiration) {
        try {
            this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET debe ser una clave Base64 de al menos 256 bits", exception);
        }
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(User user) {
        return generateToken(user, ACCESS_TOKEN, accessTokenExpiration);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_TOKEN, refreshTokenExpiration);
    }

    public long getAccessTokenExpiration() { return accessTokenExpiration; }
    public long getRefreshTokenExpiration() { return refreshTokenExpiration; }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isAccessTokenValid(String token, UserDetails user) {
        return isTokenValid(token, user, ACCESS_TOKEN);
    }

    public boolean isRefreshTokenValid(String token, UserDetails user) {
        return isTokenValid(token, user, REFRESH_TOKEN);
    }

    private boolean isTokenValid(String token, UserDetails user, String expectedType) {
        try {
            return user.getUsername().equals(extractUsername(token))
                    && expectedType.equals(extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class)))
                    && !isExpired(token);
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private String generateToken(User user, String tokenType, long expiration) {
        Date issuedAt = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .claim("role", user.getRole().name())
                .issuedAt(issuedAt)
                .expiration(new Date(issuedAt.getTime() + expiration))
                .signWith(signingKey)
                .compact();
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parse(token).getPayload());
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
    }
}
