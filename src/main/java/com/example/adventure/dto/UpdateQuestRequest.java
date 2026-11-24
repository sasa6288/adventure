package com.example.adventure.dto;

import com.example.adventure.model.QuestDifficulty;
import com.example.adventure.model.QuestStatus;
import com.example.adventure.model.QuestType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateQuestRequest(
        @NotBlank(message = "任务标题不能为空")
        @Size(max = 100, message = "任务标题不能超过100个字符")
        String title,

        @Size(max = 1000, message = "任务描述不能超过1000个字符")
        String description,

        @NotNull(message = "任务类型不能为空")
        QuestType type,

        @NotNull(message = "任务难度不能为空")
        QuestDifficulty difficulty,

        @NotNull(message = "任务状态不能为空")
        QuestStatus status,

        @Future(message = "截止时间必须是未来时间")
        LocalDateTime deadline
) {
}
