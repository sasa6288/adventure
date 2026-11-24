package com.example.adventure.security;

import com.example.adventure.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtService {
    private static final String ENCODED_HEADER = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}"
                    .getBytes(StandardCharsets.UTF_8));
    private static final Pattern SUBJECT_PATTERN =
            Pattern.compile("\\\"sub\\\"\\s*:\\s*\\\"([A-Za-z0-9_]+)\\\"");
    private static final Pattern EXPIRATION_PATTERN =
            Pattern.compile("\\\"exp\\\"\\s*:\\s*(\\d+)");

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-seconds}") long expirationSeconds) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(User user) {
        long now = Instant.now().getEpochSecond();
        String claims = "{\"sub\":\"" + user.getUsername()
                + "\",\"uid\":" + user.getId()
                + ",\"role\":\"" + user.getRole().name()
                + "\",\"iat\":" + now
                + ",\"exp\":" + (now + expirationSeconds) + "}";
        String payload = encode(claims.getBytes(StandardCharsets.UTF_8));
        String unsigned = ENCODED_HEADER + "." + payload;
        return unsigned + "." + encode(sign(unsigned));
    }

    public String validateAndGetUsername(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) throw new IllegalArgumentException("JWT格式错误");
            byte[] actual = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expected = sign(parts[0] + "." + parts[1]);
            if (!java.security.MessageDigest.isEqual(actual, expected)) {
                throw new IllegalArgumentException("JWT签名无效");
            }
            String claims = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Matcher subjectMatcher = SUBJECT_PATTERN.matcher(claims);
            Matcher expirationMatcher = EXPIRATION_PATTERN.matcher(claims);
            if (!subjectMatcher.find() || !expirationMatcher.find()
                    || Long.parseLong(expirationMatcher.group(1)) <= Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("JWT无效或已过期");
            }
            return subjectMatcher.group(1);
        } catch (Exception exception) {
            throw new IllegalArgumentException("JWT无效或已过期", exception);
        }
    }

    public long getExpirationSeconds() { return expirationSeconds; }
    public long remainingSeconds(String token) {
        validateAndGetUsername(token);
        String claims = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        Matcher matcher = EXPIRATION_PATTERN.matcher(claims);
        if (!matcher.find()) throw new IllegalArgumentException("JWT无效");
        return Long.parseLong(matcher.group(1)) - Instant.now().getEpochSecond();
    }

    private byte[] sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("无法计算JWT签名", exception);
        }
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
