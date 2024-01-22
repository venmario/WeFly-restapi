package com.example.wefly_app.test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Date;

public class JWTGenerator {
    public static void main(String[] args) {
        // Define token issuer and expiration time
        String issuer = "your-issuer";
        long expirationMillis = System.currentTimeMillis() + (60 * 60 * 1000); // 1 hour from now

        // Create and sign a token
        String token = JWT.create()
                .withIssuer(issuer)
                .withExpiresAt(new Date(expirationMillis))
                .withSubject("example-user")
                // Add more claims as needed
                .sign(Algorithm.HMAC256("your-secret")); // Use a strong secret key

        System.out.println("JWT Token: " + token);
    }
}
