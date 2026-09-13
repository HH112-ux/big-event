package com.jh.bigevent.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private long expire;

    public String generateToken(Long userId, String username) {
        return JWT.create()
                .withClaim("id", userId)
                .withClaim("username", username)
                .withExpiresAt(new Date(System.currentTimeMillis() + expire))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
    }

    public Long getUserId(String token) {
        return parseToken(token).getClaim("id").asLong();
    }

    public String getUsername(String token) {
        return parseToken(token).getClaim("username").asString();
    }

    public Map<String, Claim> getClaims(String token) {
        return parseToken(token).getClaims();
    }
}
