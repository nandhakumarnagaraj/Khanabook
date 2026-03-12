package com.khanabook.saas.utility;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtility {

    @Value("${jwt.secret:YourDefaultSecretKeyThatIsAtLeast32CharsLong}")
    private String secret;

    public Long extractRestaurantId(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("restaurantId", Long.class);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(String username, Long restaurantId) { SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); return Jwts.builder().setSubject(username).claim("restaurantId", restaurantId).setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)).signWith(key).compact(); } public Boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
