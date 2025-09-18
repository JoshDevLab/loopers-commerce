package com.loopers.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 배치에서 가중치 설정을 읽어오는 단순한 유틸리티 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class WeightConfigReader {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String WEIGHT_CONFIG_KEY = "ranking:weight:config";
    
    // 기본 가중치 설정
    private static final double DEFAULT_VIEW_WEIGHT = 0.1;
    private static final double DEFAULT_LIKE_WEIGHT = 0.2;
    private static final double DEFAULT_ORDER_WEIGHT = 0.6;

    public record WeightConfig(double viewWeight, double likeWeight, double orderWeight) {}

    /**
     * 현재 가중치 설정을 조회하거나 기본값을 반환
     */
    public WeightConfig getCurrentWeights() {
        try {
            // Redis에서 가중치 설정 조회 시도
            Object configObj = redisTemplate.opsForValue().get(WEIGHT_CONFIG_KEY);
            
            if (configObj instanceof WeightConfig config) {
                log.debug("Redis에서 가중치 설정 조회 성공: view={}, like={}, order={}", 
                         config.viewWeight(), config.likeWeight(), config.orderWeight());
                return config;
            }
            
            // 다른 모듈의 WeightConfig 객체인 경우 처리
            if (configObj != null) {
                try {
                    // 리플렉션으로 값 추출 (다른 모듈과의 호환성)
                    Object viewWeight = configObj.getClass().getMethod("getViewWeight").invoke(configObj);
                    Object likeWeight = configObj.getClass().getMethod("getLikeWeight").invoke(configObj);
                    Object orderWeight = configObj.getClass().getMethod("getOrderWeight").invoke(configObj);
                    
                    double view = ((Number) viewWeight).doubleValue();
                    double like = ((Number) likeWeight).doubleValue(); 
                    double order = ((Number) orderWeight).doubleValue();
                    
                    WeightConfig config = new WeightConfig(view, like, order);
                    log.debug("다른 모듈의 WeightConfig 변환 성공: {}", config);
                    return config;
                } catch (Exception e) {
                    log.warn("WeightConfig 변환 실패", e);
                }
            }
        } catch (Exception e) {
            log.warn("Redis에서 가중치 설정 조회 실패", e);
        }
        
        // 기본값 반환
        WeightConfig defaultConfig = new WeightConfig(DEFAULT_VIEW_WEIGHT, DEFAULT_LIKE_WEIGHT, DEFAULT_ORDER_WEIGHT);
        log.info("기본 가중치 설정 사용: {}", defaultConfig);
        return defaultConfig;
    }
}
