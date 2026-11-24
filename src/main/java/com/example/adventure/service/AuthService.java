package com.example.adventure.service;

import com.example.adventure.dto.*;
import com.example.adventure.exception.InvalidCredentialsException;
import com.example.adventure.exception.UsernameAlreadyExistsException;
import com.example.adventure.model.User;
import com.example.adventure.model.UserRole;
import com.example.adventure.repository.UserRepository;
import com.example.adventure.security.AuthenticatedUser;
import com.example.adventure.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) throw new UsernameAlreadyExistsException();
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname().trim());
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setLevel(1);
        user.setExperience(0);
        user.setCoins(0);
        return UserService.toResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            User user = ((AuthenticatedUser) authentication.getPrincipal()).user();
            return new AuthResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationSeconds());
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }
    }
}
