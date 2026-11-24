package com.example.adventure.controller;

import com.example.adventure.service.RedisFeatures;
import com.example.adventure.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RedisAuthController {
    private final RedisFeatures redis;
    private final JwtService jwt;
    private final boolean exposeCode;
    public RedisAuthController(RedisFeatures redis, JwtService jwt,
            @Value("${app.verification.expose-code:false}") boolean exposeCode) {
        this.redis=redis; this.jwt=jwt; this.exposeCode=exposeCode;
    }
    public record EmailRequest(@NotBlank @Email String email) {}
    public record CodeRequest(@NotBlank @Email String email, @NotBlank @Pattern(regexp="[0-9]{6}") String code) {}
    @PostMapping("/verification-codes")
    public Map<String,Object> send(@Valid @RequestBody EmailRequest request) {
        if (!exposeCode) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "尚未配置邮件发送服务，请在本地学习环境启用验证码演示模式");
        return Map.of("code", redis.issueCode(request.email()), "expiresIn", 300);
    }
    @PostMapping("/verification-codes/verify")
    public Map<String,Boolean> verify(@Valid @RequestBody CodeRequest request) {
        if (!redis.verifyCode(request.email(), request.code())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"验证码错误或已过期");
        return Map.of("verified",true);
    }
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authorization) {
        if (!authorization.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String token=authorization.substring(7);
        jwt.validateAndGetUsername(token);
        redis.revoke(token,jwt.remainingSeconds(token));
    }
}
