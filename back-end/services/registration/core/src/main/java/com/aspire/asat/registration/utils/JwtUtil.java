package com.aspire.asat.registration.utils;

import com.aspire.asat.registration.model.Client;
import com.aspire.asat.registration.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public static String generateToken(User user, int days) {

        // Requirement

//        {
//            "id":"1212121",
//                "email": "user@example.com",
//                "role": "USER",
//                "pur": "reg",
//                "iat": 1670608800,
//                "exp": 1700000000
//        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());

        long issuedAt = System.currentTimeMillis();

        long expiration = issuedAt + TimeUnit.DAYS.toMillis(days);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(issuedAt))
                .setExpiration(new Date(expiration))
                .signWith(SECRET_KEY)
                .compact();
    }
    public static String generateToken(Client client, int days) {

        // Requirement

//        {
//            "id":"1212121",
//                "email": "user@example.com",
//                "role": "USER",
//                "pur": "reg",
//                "iat": 1670608800,
//                "exp": 1700000000
//        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", client.getId());
        claims.put("email", client.getEmail());
        claims.put("role", client.getRole());

        long issuedAt = System.currentTimeMillis();

        long expiration = issuedAt + TimeUnit.DAYS.toMillis(days);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(issuedAt))
                .setExpiration(new Date(expiration))
                .signWith(SECRET_KEY)
                .compact();
    }


    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

}
