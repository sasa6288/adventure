package com.example.adventure.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("用户名或密码错误，或者用户已被禁用");
    }
}
