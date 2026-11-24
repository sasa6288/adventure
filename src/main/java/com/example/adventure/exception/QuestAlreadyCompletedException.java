package com.example.adventure.exception;

public class QuestAlreadyCompletedException extends RuntimeException {

    public QuestAlreadyCompletedException(Long id) {
        super("ID为 " + id + " 的任务已经完成，不能重复完成");
    }
}
