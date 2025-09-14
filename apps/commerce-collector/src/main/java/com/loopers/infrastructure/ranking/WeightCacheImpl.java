package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeightCache;
import com.loopers.domain.ranking.WeightConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class WeightCacheImpl implements WeightCache {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String WEIGHT_CONFIG_KEY = "ranking:weight:config";

    @Override
    public void saveWeightConfig(WeightConfig weightConfig) {
        try {
            Map<String, String> weights = Map.of(
                    "view", String.valueOf(weightConfig.getViewWeight()),
                    "like", String.valueOf(weightConfig.getLikeWeight()),
                    "order", String.valueOf(weightConfig.getOrderWeight())
            );

            redisTemplate.opsForHash().putAll(WEIGHT_CONFIG_KEY, weights);

            log.info("가중치 설정 저장 완료: {}", weightConfig);
        } catch (Exception e) {
            log.error("가중치 설정 저장 실패", e);
            throw new WeightCacheException("가중치 설정 저장 실패", e);
        }
    }

    @Override
    public WeightConfig getWeightConfig() {
        try {
            Map<Object, Object> weights = redisTemplate.opsForHash().entries(WEIGHT_CONFIG_KEY);

            if (weights.isEmpty()) {
                return null;
            }

            WeightConfig config = new WeightConfig(
                    Double.parseDouble((String) weights.get("view")),
                    Double.parseDouble((String) weights.get("like")),
                    Double.parseDouble((String) weights.get("order"))
            );

            log.debug("가중치 설정 조회: {}", config);
            return config;

        } catch (Exception e) {
            log.error("가중치 설정 조회 실패", e);
            return null;
        }
    }

    public static class WeightCacheException extends RuntimeException {
        public WeightCacheException(String message, Exception e) {
            super(message, e);
        }
    }
}
