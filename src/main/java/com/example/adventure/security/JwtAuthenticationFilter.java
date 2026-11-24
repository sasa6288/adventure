package com.example.adventure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.adventure.web.ErrorResponseWriter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ErrorResponseWriter errorWriter;
    private final com.example.adventure.service.RedisFeatures redis;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService,
                                   ErrorResponseWriter errorWriter, com.example.adventure.service.RedisFeatures redis) {
        this.redis = redis;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.errorWriter = errorWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if ("POST".equals(request.getMethod()) && "/api/auth/login".equals(request.getServletPath())) {
            try { redis.limit("login:" + request.getRemoteAddr(), 10, 60); }
            catch (org.springframework.web.server.ResponseStatusException ex) {
                errorWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS, "登录请求过于频繁"); return;
            }
        }
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.validateAndGetUsername(authorization.substring(7));
            if (redis.revoked(authorization.substring(7))) throw new IllegalArgumentException("Token已退出登录");
            UserDetails details = userDetailsService.loadUserByUsername(username);
            if (!details.isEnabled()) {
                errorWriter.write(request, response, HttpStatus.UNAUTHORIZED, "用户已被禁用");
                return;
            }
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            errorWriter.write(request, response, HttpStatus.UNAUTHORIZED, "JWT无效或已过期");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
