package com.example.adventure.exception;

public class AlreadyCheckedInException extends RuntimeException {
    public AlreadyCheckedInException() { super("今天已经签到，请勿重复签到"); }
}
