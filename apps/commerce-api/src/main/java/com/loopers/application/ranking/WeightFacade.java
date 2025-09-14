package com.loopers.application.ranking;

import com.loopers.domain.ranking.WeightConfigInfo;
import com.loopers.domain.ranking.WeightConfigService;
import com.loopers.domain.ranking.WeightResetCommand;
import com.loopers.domain.ranking.WeightUpdateCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class WeightFacade {
    private final WeightConfigService weightConfigService;

    public void updateWeights(WeightUpdateCommand command) {
        log.info("가중치 업데이트 요청: view={}, like={}, order={}",
                command.viewWeight(), command.likeWeight(), command.orderWeight());

        weightConfigService.updateWeights(
                command.viewWeight(),
                command.likeWeight(),
                command.orderWeight()
        );

        log.info("가중치 업데이트 완료");
    }

    public WeightConfigInfo getCurrentWeights() {
        return weightConfigService.getCurrentWeights();
    }

    public void resetWeights(WeightResetCommand command) {
        log.info("가중치 초기화 요청: reason={}", command.reason());

        weightConfigService.resetToDefault(command.reason());

        log.info("가중치 초기화 완료");
    }
}
