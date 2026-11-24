package com.example.adventure.exception;

public class QuestNotFoundException extends RuntimeException {

    public QuestNotFoundException(Long id) {
        super("没有找到ID为 " + id + " 的冒险任务");
    }
}
