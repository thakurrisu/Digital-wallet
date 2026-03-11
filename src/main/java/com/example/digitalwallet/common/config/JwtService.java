package com.example.digitalwallet.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    private static final String SECRET_HEX =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private static final long JWT_EXPIRATION = 86400000L; // 24 hours in ms

    private  SecretKey secretKey;

    public JwtService() {
        this.secretKey = Keys.hmacShaKeyFor(
                hexStringToByteArray(SECRET_HEX));
    }

   private String generateToken(UUID id, String email){
       Date issuedAt = new Date();
       Date expiry = new Date(issuedAt.getTime() + JWT_EXPIRATION);
       return Jwts.builder()
               .subject(id.toString())
               .claim("email",email)
               .issuedAt(issuedAt)
               .expiration(expiry).signWith(secretKey).compact();
   }

   private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                // verifyWith tells parser: use THIS key to verify
                // parser will:
                // 1. Split token → header, payload, signature
                // 2. Read header → "alg":"HS256"
                //    (but trusts OUR key type, not header's alg claim)
                // 3. Recalculate: HMAC_SHA256(header.payload, secretKey)
                // 4. Compare with token's signature
                // 5. Mismatch → SignatureException
                // 6. Decode payload → check exp timestamp
                // 7. Expired → ExpiredJwtException
                // 8. All good → return Claims
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    public boolean isTokenValid(String token){
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }


    private byte[] hexStringToByteArray(String hex) {
        // Converts "404E6352..." → [0x40, 0x4E, 0x63, 0x52...]
        // Each pair of hex chars = one byte
        // "40" → 64 decimal → 0x40 → one byte
        // 64 hex chars → 32 bytes → 256 bits → valid HS256 key
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public UUID extractUserId(String jwt) {
      return  UUID.fromString(extractAllClaims(jwt).getSubject());
    }

    public String extractEmail(String jwt) {
        return extractAllClaims(jwt).get("email", String.class);
    }
}
