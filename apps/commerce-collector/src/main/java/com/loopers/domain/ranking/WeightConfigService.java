package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class WeightConfigService {

    private final WeightCache weightCache;

    private static final double DEFAULT_VIEW_WEIGHT = 0.1;
    private static final double DEFAULT_LIKE_WEIGHT = 0.2;
    private static final double DEFAULT_ORDER_WEIGHT = 0.6;

    public void updateWeights(WeightConfig weightConfig) {
        weightConfig.validate();

        weightCache.saveWeightConfig(weightConfig);

        log.info("랭킹 가중치 업데이트 완료: {}", weightConfig);
    }

    public WeightConfig getCurrentWeights() {
        WeightConfig config = weightCache.getWeightConfig();

        if (config == null) {
            log.info("저장된 가중치 설정이 없어 기본값 사용");
            return getDefaultWeightConfig();
        }

        return config;
    }

    private WeightConfig getDefaultWeightConfig() {
        return new WeightConfig(DEFAULT_VIEW_WEIGHT, DEFAULT_LIKE_WEIGHT, DEFAULT_ORDER_WEIGHT);
    }

    public void resetToDefault() {
        updateWeights(getDefaultWeightConfig());
        log.info("랭킹 가중치를 기본값으로 초기화");
    }

}
