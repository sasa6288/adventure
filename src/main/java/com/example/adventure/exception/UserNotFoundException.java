package com.example.adventure.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("没有找到ID为 " + id + " 的用户");
    }

    public UserNotFoundException(String username) {
        super("没有找到用户 " + username);
    }
}
