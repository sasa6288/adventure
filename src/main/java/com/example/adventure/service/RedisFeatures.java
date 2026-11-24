package com.example.adventure.service;

import java.time.Duration;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class RedisFeatures {
    private final StringRedisTemplate redis;
    private final SecureRandom random = new SecureRandom();
    private static final DefaultRedisScript<Long> COUNTER = new DefaultRedisScript<>(
        "local n=redis.call('INCR',KEYS[1]); if n==1 then redis.call('EXPIRE',KEYS[1],ARGV[1]) end; return n", Long.class);
    private static final DefaultRedisScript<Long> VERIFY = new DefaultRedisScript<>(
        "local v=redis.call('GET',KEYS[1]); if not v then return 0 end; local n=redis.call('INCR',KEYS[2]); if n==1 then redis.call('EXPIRE',KEYS[2],300) end; if n>5 then redis.call('DEL',KEYS[1]); return 0 end; if v==ARGV[1] then redis.call('DEL',KEYS[1],KEYS[2]); return 1 end; return 0", Long.class);

    public RedisFeatures(StringRedisTemplate redis) { this.redis = redis; }
    public void limit(String key, int max, int seconds) {
        Long count = redis.execute(COUNTER, List.of("adventure:limit:" + hash(key)), String.valueOf(seconds));
        if (count == null || count > max) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "请求过于频繁，请稍后重试");
    }
    public String issueCode(String email) {
        String id = hash(email.toLowerCase(Locale.ROOT));
        limit("code:send:" + id, 1, 60);
        String code = String.format("%06d", random.nextInt(1000000));
        redis.opsForValue().set("adventure:code:" + id, hash(code), Duration.ofMinutes(5));
        redis.delete("adventure:code-attempt:" + id);
        return code;
    }
    public boolean verifyCode(String email, String code) {
        String id = hash(email.toLowerCase(Locale.ROOT));
        return Long.valueOf(1).equals(redis.execute(VERIFY,
            List.of("adventure:code:" + id, "adventure:code-attempt:" + id), hash(code)));
    }
    public boolean revoked(String token) { return Boolean.TRUE.equals(redis.hasKey("adventure:revoked:" + hash(token))); }
    public void revoke(String token, long seconds) {
        if (seconds > 0) redis.opsForValue().set("adventure:revoked:" + hash(token), "1", Duration.ofSeconds(seconds));
    }
    private String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
    }
}
