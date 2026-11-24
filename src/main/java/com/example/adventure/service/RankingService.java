package com.example.adventure.service;

import com.example.adventure.dto.RankingResponse;
import com.example.adventure.model.User;
import com.example.adventure.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RankingService {
    private final UserRepository userRepository;
    private final org.springframework.data.redis.core.StringRedisTemplate redis;
    private final tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
    public RankingService(UserRepository userRepository, org.springframework.data.redis.core.StringRedisTemplate redis) {
        this.userRepository = userRepository; this.redis = redis;
    }

    public List<RankingResponse> findRankings(int limit) {
        if (limit < 1 || limit > 100) throw new IllegalArgumentException("排行榜数量必须在1到100之间");
        String key = "adventure:ranking:" + limit;
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) return Arrays.asList(mapper.readValue(cached, RankingResponse[].class));
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("排行榜缓存读取失败，查询数据库");
        }
        List<User> users = userRepository.findRanking(PageRequest.of(0, limit));
        List<RankingResponse> result = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            result.add(new RankingResponse(i + 1, u.getId(), u.getUsername(), u.getNickname(),
                    u.getLevel(), u.getExperience(), u.getCoins()));
        }
        try { redis.opsForValue().set(key, mapper.writeValueAsString(result), java.time.Duration.ofSeconds(30)); }
        catch (Exception ex) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("排行榜缓存写入失败"); }
        return result;
    }
}
