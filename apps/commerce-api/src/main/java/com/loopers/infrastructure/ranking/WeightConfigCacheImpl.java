package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeightConfig;
import com.loopers.domain.ranking.WeightConfigCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class WeightConfigCacheImpl implements WeightConfigCache {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String WEIGHT_CONFIG_KEY = "ranking:weight:config";

    @Override
    public WeightConfig getWeightConfig() {
            Map<Object, Object> weights = redisTemplate.opsForHash().entries(WEIGHT_CONFIG_KEY);

            if (weights.isEmpty()) {
                log.debug("Redis에 저장된 가중치 설정이 없음");
                return null;
            }

            WeightConfig config = new WeightConfig(
                    Double.parseDouble((String) weights.get("view")),
                    Double.parseDouble((String) weights.get("like")),
                    Double.parseDouble((String) weights.get("order"))
            );

            log.debug("Redis에서 가중치 설정 조회: {}", config);
            return config;

    }
}
