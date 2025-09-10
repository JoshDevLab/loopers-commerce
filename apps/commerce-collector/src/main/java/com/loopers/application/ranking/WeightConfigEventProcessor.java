package com.loopers.application.ranking;

import com.loopers.domain.ranking.WeightConfig;
import com.loopers.domain.ranking.WeightConfigService;
import com.loopers.domain.ranking.WeightConfigUpdateCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class WeightConfigEventProcessor {

    private final WeightConfigService weightConfigService;

    public void processEvent(WeightConfigUpdateCommand command) {
        if (command == null) {
            log.warn("WeightConfigUpdateCommand가 null입니다.");
            return;
        }

        log.info("가중치 변경 명령 처리 시작: eventId={}, view={}, like={}, order={}",
                command.getEventId(), command.getViewWeight(),
                command.getLikeWeight(), command.getOrderWeight());

        WeightConfig weightConfig = new WeightConfig(
                command.getViewWeight(),
                command.getLikeWeight(),
                command.getOrderWeight()
        );

        weightConfigService.updateWeights(weightConfig);

        log.info("가중치 변경 명령 처리 완료: eventId={}, config={}",
                command.getEventId(), weightConfig);

    }
}
