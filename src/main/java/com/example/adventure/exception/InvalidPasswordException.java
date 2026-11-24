package com.example.adventure.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("当前密码不正确");
    }
}
