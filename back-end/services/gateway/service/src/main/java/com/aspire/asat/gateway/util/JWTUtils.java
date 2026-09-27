package com.aspire.asat.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class JWTUtils {
    private JWTUtils() {
    }

    public static String extractUserId(String token, String jwtSecretKey) {
        return extractClaim(token, jwtSecretKey, Claims::getSubject);
    }

    public static String extractTokenId(String token, String jwtSecretKey) {
        return extractClaim(token, jwtSecretKey, Claims::getSubject);
    }

    public static Date extractExpiration(String token, String jwtSecretKey) {
        return extractClaim(token, jwtSecretKey, Claims::getExpiration);
    }

    public static String trimToken(String bearerToken, String jwtTokenPrefix) {
        String tokenPrefix = String.format("%s ", jwtTokenPrefix);
        return StringUtils.replace(bearerToken, tokenPrefix, "");
    }

    public static boolean validateToken(String token, String userName, String jwtSecretKey) {
        return StringUtils.equals(extractUserId(token, jwtSecretKey), userName) && !isTokenExpired(token, jwtSecretKey);
    }

   public static List<String> getScopes(String token, String jwtSecretKey) {
        try {
            Claims claims = extractAllClaims(token, jwtSecretKey);
            String encodedScope = claims.get("scope", String.class);
            @SuppressWarnings("unchecked")
            List<String> scopes = SerializationUtils.deserialize(encodedScope, List.class);
            return scopes;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static <T> T extractClaim(String token, String jwtSecretKey, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, jwtSecretKey);
        return claimsResolver.apply(claims);
    }

    private static Claims extractAllClaims(String token, String jwtSecretKey) {
        Key key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean isTokenExpired(String token, String jwtSecretKey) {
        return extractExpiration(token, jwtSecretKey).before(new Date());
    }
}
