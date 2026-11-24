package com.example.adventure.service;

import com.example.adventure.dto.CreateQuestRequest;
import com.example.adventure.dto.PageResponse;
import com.example.adventure.dto.QuestResponse;
import com.example.adventure.dto.UpdateQuestRequest;
import com.example.adventure.exception.QuestAlreadyCompletedException;
import com.example.adventure.exception.QuestNotFoundException;
import com.example.adventure.model.Quest;
import com.example.adventure.model.QuestDifficulty;
import com.example.adventure.model.QuestStatus;
import com.example.adventure.model.User;
import com.example.adventure.model.UserRole;
import com.example.adventure.repository.QuestRepository;
import com.example.adventure.repository.UserRepository;
import com.example.adventure.exception.UserNotFoundException;
import com.example.adventure.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final GameProgressService gameProgressService;
    private final AchievementService achievementService;

    public QuestService(QuestRepository questRepository, UserRepository userRepository,
                        GameProgressService gameProgressService, AchievementService achievementService) {
        this.questRepository = questRepository;
        this.userRepository = userRepository;
        this.gameProgressService = gameProgressService;
        this.achievementService = achievementService;
    }

    public PageResponse<QuestResponse> findAll(
            int page,
            int size,
            QuestStatus status,
            String keyword
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("每页数量必须在1到100之间");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        User user = currentUser();
        Page<Quest> quests = user.getRole() == UserRole.ADMIN
                ? questRepository.search(status, normalizeKeyword(keyword), pageable)
                : questRepository.searchByUserId(user.getId(), status, normalizeKeyword(keyword), pageable);
        return toPageResponse(quests);
    }

    public QuestResponse findById(Long id) {
        return toResponse(findQuest(id));
    }

    public QuestResponse create(CreateQuestRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Reward reward = calculateReward(request.difficulty());

        Quest quest = new Quest(
                null,
                request.title(),
                request.description(),
                request.type(),
                request.difficulty(),
                QuestStatus.TODO,
                reward.experience(),
                reward.coins(),
                request.deadline(),
                now,
                now
        );
        quest.setUser(currentUser());

        Quest savedQuest = questRepository.save(quest);
        return toResponse(savedQuest);
    }

    public QuestResponse update(Long id, UpdateQuestRequest request) {
        Quest quest = findQuest(id);

        quest.setTitle(request.title());
        quest.setDescription(request.description());
        quest.setType(request.type());
        quest.setDifficulty(request.difficulty());
        quest.setStatus(request.status());
        quest.setDeadline(request.deadline());

        Reward reward = calculateReward(request.difficulty());
        quest.setRewardExperience(reward.experience());
        quest.setRewardCoins(reward.coins());
        quest.setUpdatedAt(LocalDateTime.now());

        return toResponse(questRepository.save(quest));
    }

    @Transactional
    public QuestResponse completeQuest(Long id, Long currentUserId) {
        // 锁住用户与任务，保证并发请求不会重复发放奖励。
        User user = userRepository.findByIdForUpdate(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
        Quest quest = questRepository.findByIdAndUserIdForUpdate(id, currentUserId)
                .orElseThrow(() -> new QuestNotFoundException(id));
        if (quest.getStatus() == QuestStatus.COMPLETED) {
            throw new QuestAlreadyCompletedException(id);
        }
        quest.setStatus(QuestStatus.COMPLETED);
        quest.setUpdatedAt(LocalDateTime.now());
        gameProgressService.addRewards(user, quest.getRewardExperience(), quest.getRewardCoins());
        questRepository.save(quest);
        userRepository.save(user);
        long completedCount = questRepository.countByUserIdAndStatus(currentUserId, QuestStatus.COMPLETED);
        achievementService.checkQuestAchievements(user, completedCount);
        return toResponse(quest);
    }

    public void delete(Long id) {
        Quest quest = findQuest(id);
        questRepository.delete(quest);
    }

    private Quest findQuest(Long id) {
        User user = currentUser();
        return (user.getRole() == UserRole.ADMIN
                ? questRepository.findById(id)
                : questRepository.findByIdAndUserId(id, user.getId()))
                .orElseThrow(() -> new QuestNotFoundException(id));
    }

    private User currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((AuthenticatedUser) principal).user();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private PageResponse<QuestResponse> toPageResponse(Page<Quest> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private Reward calculateReward(QuestDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> new Reward(20, 10);
            case MEDIUM -> new Reward(50, 25);
            case HARD -> new Reward(100, 50);
        };
    }

    private QuestResponse toResponse(Quest quest) {
        return new QuestResponse(
                quest.getId(),
                quest.getTitle(),
                quest.getDescription(),
                quest.getType(),
                quest.getDifficulty(),
                quest.getStatus(),
                quest.getRewardExperience(),
                quest.getRewardCoins(),
                quest.getDeadline(),
                quest.getCreatedAt(),
                quest.getUpdatedAt()
        );
    }

    private record Reward(int experience, int coins) {
    }
}
