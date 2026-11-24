package com.example.adventure;

import com.example.adventure.model.*;
import com.example.adventure.repository.*;
import com.example.adventure.security.JwtService;
import com.example.adventure.service.AchievementService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GameMechanicsIntegrationTests {
    private static final String SECRET =
            "test-secret-that-is-definitely-longer-than-thirty-two-characters";

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @Autowired UserRepository userRepository;
    @Autowired QuestRepository questRepository;
    @Autowired AchievementRepository achievementRepository;
    @Autowired UserAchievementRepository userAchievementRepository;
    @Autowired CheckInRepository checkInRepository;
    @MockitoSpyBean AchievementService achievementService;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        userAchievementRepository.deleteAll();
        checkInRepository.deleteAll();
        questRepository.deleteAll();
        achievementRepository.deleteAll();
        userRepository.deleteAll();
        seedAchievements();
        user = saveUser("alice", UserRole.USER, 0, 1);
        token = jwtService.generateToken(user);
    }

    @AfterEach
    void tearDown() {
        reset(achievementService);
    }

    @Test
    void createsQuestNormally() throws Exception {
        mockMvc.perform(post("/api/quests")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validQuestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("学习事务"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.rewardExperience").value(20))
                .andExpect(jsonPath("$.rewardCoins").value(10));
        assertThat(questRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        String body = questJson("   ", LocalDateTime.now().plusDays(1));
        mockMvc.perform(post("/api/quests").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists());
        assertThat(questRepository.count()).isZero();
    }

    @Test
    void rejectsPastDeadline() throws Exception {
        String body = questJson("过期任务", LocalDateTime.now().minusMinutes(1));
        mockMvc.perform(post("/api/quests").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.deadline").exists());
    }

    @Test
    void returnsNotFoundForMissingQuest() throws Exception {
        mockMvc.perform(get("/api/quests/999999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsCompletingQuestTwice() throws Exception {
        Quest quest = saveQuest(user, QuestStatus.TODO, 20, 10);
        complete(quest.getId(), token).andExpect(status().isOk());
        complete(quest.getId(), token).andExpect(status().isConflict());
        User refreshed = userRepository.findById(user.getId()).orElseThrow();
        assertThat(refreshed.getExperience()).isEqualTo(20);
        assertThat(refreshed.getCoins()).isEqualTo(10);
    }

    @Test
    void completingQuestAddsExperienceAndCoins() throws Exception {
        Quest quest = saveQuest(user, QuestStatus.TODO, 50, 25);
        complete(quest.getId(), token).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        User refreshed = userRepository.findById(user.getId()).orElseThrow();
        assertThat(refreshed.getExperience()).isEqualTo(50);
        assertThat(refreshed.getCoins()).isEqualTo(25);
    }

    @Test
    void levelsUpWhenExperienceReachesThreshold() throws Exception {
        user.setExperience(90);
        userRepository.save(user);
        Quest quest = saveQuest(user, QuestStatus.TODO, 20, 10);
        complete(quest.getId(), token).andExpect(status().isOk());
        User refreshed = userRepository.findById(user.getId()).orElseThrow();
        assertThat(refreshed.getExperience()).isEqualTo(110);
        assertThat(refreshed.getLevel()).isEqualTo(2);
    }

    @Test
    void unauthenticatedUserCannotAccessQuests() throws Exception {
        mockMvc.perform(get("/api/quests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotAccessAnotherUsersQuest() throws Exception {
        User bob = saveUser("bob", UserRole.USER, 0, 1);
        Quest quest = saveQuest(bob, QuestStatus.TODO, 20, 10);
        mockMvc.perform(get("/api/quests/{id}", quest.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
        complete(quest.getId(), token).andExpect(status().isNotFound());
    }

    @Test
    void normalUserCannotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void expiredJwtIsRejected() throws Exception {
        String expiredToken = new JwtService(SECRET, -1).generateToken(user);
        mockMvc.perform(get("/api/quests").header("Authorization", bearer(expiredToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rollsBackQuestAndRewardsWhenRewardFlowFails() throws Exception {
        Quest quest = saveQuest(user, QuestStatus.TODO, 100, 50);
        doThrow(new IllegalStateException("模拟奖励发放失败"))
                .when(achievementService).checkQuestAchievements(any(User.class), anyLong());

        complete(quest.getId(), token).andExpect(status().isInternalServerError());

        Quest refreshedQuest = questRepository.findById(quest.getId()).orElseThrow();
        User refreshedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(refreshedQuest.getStatus()).isEqualTo(QuestStatus.TODO);
        assertThat(refreshedUser.getExperience()).isZero();
        assertThat(refreshedUser.getCoins()).isZero();
        assertThat(refreshedUser.getLevel()).isEqualTo(1);
    }

    private org.springframework.test.web.servlet.ResultActions complete(Long questId, String jwt)
            throws Exception {
        return mockMvc.perform(patch("/api/quests/{id}/complete", questId)
                .header("Authorization", bearer(jwt)));
    }

    private String validQuestJson() throws Exception {
        return questJson("学习事务", LocalDateTime.now().plusDays(1));
    }

    private String questJson(String title, LocalDateTime deadline) {
        return """
                {"title":"%s","description":"test","type":"STUDY",
                 "difficulty":"EASY","deadline":"%s"}
                """.formatted(title, deadline);
    }

    private User saveUser(String username, UserRole role, int experience, int level) {
        User value = new User();
        value.setUsername(username);
        value.setPassword("{noop}password");
        value.setNickname(username);
        value.setRole(role);
        value.setEnabled(true);
        value.setExperience(experience);
        value.setLevel(level);
        value.setCoins(0);
        return userRepository.saveAndFlush(value);
    }

    private Quest saveQuest(User owner, QuestStatus status, int experience, int coins) {
        LocalDateTime now = LocalDateTime.now();
        Quest quest = new Quest(null, "测试任务", "测试", QuestType.STUDY, QuestDifficulty.EASY,
                status, experience, coins, now.plusDays(1), now, now);
        quest.setUser(owner);
        return questRepository.saveAndFlush(quest);
    }

    private void seedAchievements() {
        achievementRepository.save(new Achievement(AchievementType.FIRST_QUEST, "初次冒险", "首次完成任务"));
        achievementRepository.save(new Achievement(AchievementType.QUEST_MASTER, "任务达人", "完成10个任务"));
        achievementRepository.save(new Achievement(AchievementType.LEVEL_FIVE, "渐入佳境", "达到5级"));
        achievementRepository.save(new Achievement(AchievementType.SEVEN_DAY_STREAK, "持之以恒", "连续签到7天"));
    }

    private String bearer(String jwt) { return "Bearer " + jwt; }
}
