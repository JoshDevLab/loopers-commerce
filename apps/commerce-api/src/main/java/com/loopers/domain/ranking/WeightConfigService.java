package com.loopers.domain.ranking;

import com.loopers.domain.outbox.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class WeightConfigService {

    private final OutboxEventPublisher eventPublisher;
    private final WeightConfigCache weightConfigCache;

    private static final double DEFAULT_VIEW_WEIGHT = 0.1;
    private static final double DEFAULT_LIKE_WEIGHT = 0.2;
    private static final double DEFAULT_ORDER_WEIGHT = 0.6;

    @Transactional
    public void updateWeights(double viewWeight, double likeWeight, double orderWeight) {
        WeightConfig.validate(viewWeight, likeWeight, orderWeight);

        WeightConfigChangedEvent event = new WeightConfigChangedEvent(
                viewWeight, likeWeight, orderWeight
        );

        eventPublisher.publish(event);

        log.info("랭킹 가중치 변경 이벤트 발행 완료: view={}, like={}, order={}",
                viewWeight, likeWeight, orderWeight);
    }

    @Transactional
    public void resetToDefault(String reason) {
        WeightConfigChangedEvent event = new WeightConfigChangedEvent(
                DEFAULT_VIEW_WEIGHT, DEFAULT_LIKE_WEIGHT, DEFAULT_ORDER_WEIGHT
        );

        eventPublisher.publish(event);

        log.info("랭킹 가중치 초기화 이벤트 발행 완료: reason={}", reason);
    }

    @Transactional
    public WeightConfigInfo getCurrentWeights() {
        WeightConfig currentConfig = weightConfigCache.getWeightConfig();
        if (currentConfig == null) {
            WeightConfig defaultConfig = WeightConfig.getDefaultWeightConfig(
                    DEFAULT_VIEW_WEIGHT, DEFAULT_LIKE_WEIGHT, DEFAULT_ORDER_WEIGHT);

            return new WeightConfigInfo(
                    defaultConfig.getViewWeight(),
                    defaultConfig.getLikeWeight(),
                    defaultConfig.getOrderWeight()
            );
        }

        return new WeightConfigInfo(
                currentConfig.getViewWeight(),
                currentConfig.getLikeWeight(),
                currentConfig.getOrderWeight()
        );
    }

}
