package com.example.adventure.service;

import com.example.adventure.dto.ChangePasswordRequest;
import com.example.adventure.dto.UpdateUserRequest;
import com.example.adventure.dto.UserResponse;
import com.example.adventure.exception.InvalidPasswordException;
import com.example.adventure.exception.UserNotFoundException;
import com.example.adventure.model.User;
import com.example.adventure.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getMe(User currentUser) {
        return toResponse(requireCurrent(currentUser));
    }

    @Transactional
    public UserResponse updateMe(User currentUser, UpdateUserRequest request) {
        User user = requireCurrent(currentUser);
        user.setNickname(request.nickname().trim());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        User user = requireCurrent(currentUser);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserService::toResponse).toList();
    }

    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setEnabled(enabled);
        return toResponse(userRepository.save(user));
    }

    private User requireCurrent(User currentUser) {
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException(currentUser.getId()));
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getNickname(), user.getRole(),
                user.isEnabled(), user.getLevel(), user.getExperience(), user.getCoins());
    }
}
