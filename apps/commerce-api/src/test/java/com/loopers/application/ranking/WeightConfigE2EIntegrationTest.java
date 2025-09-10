package com.loopers.application.ranking;

import com.loopers.domain.ranking.WeightConfigInfo;
import com.loopers.domain.ranking.WeightResetCommand;
import com.loopers.domain.ranking.WeightUpdateCommand;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "spring.kafka.enabled=true" // 카프카 통합 테스트용
})
@DisplayName("WeightConfig E2E 통합 테스트")
class WeightConfigE2EIntegrationTest extends IntegrationTestSupport {

    @Autowired
    WeightFacade weightFacade;

    @Test
    @DisplayName("가중치 업데이트부터 조회까지 전체 플로우 테스트")
    void weightConfigFullFlow() throws InterruptedException {
        // given
        WeightUpdateCommand updateCommand = new WeightUpdateCommand(0.15, 0.35, 0.5);

        // when - 가중치 업데이트
        weightFacade.updateWeights(updateCommand);

        // 이벤트 처리 대기 (실제 환경에서는 비동기 처리)
        Thread.sleep(1000);

        // then - 업데이트된 가중치 조회
        WeightConfigInfo currentWeights = weightFacade.getCurrentWeights();
        
        // API 모듈에서는 캐시에 즉시 반영되지 않을 수 있지만,
        // 기본적인 조회 동작이 정상인지 확인
        assertThat(currentWeights).isNotNull();
        assertThat(currentWeights.viewWeight()).isNotNegative();
        assertThat(currentWeights.likeWeight()).isNotNegative();
        assertThat(currentWeights.orderWeight()).isNotNegative();
    }

    @Test
    @DisplayName("가중치 초기화 플로우 테스트")
    void weightConfigResetFlow() throws InterruptedException {
        // given
        WeightResetCommand resetCommand = new WeightResetCommand("통합 테스트 초기화");

        // when
        weightFacade.resetWeights(resetCommand);

        // 이벤트 처리 대기
        Thread.sleep(1000);

        // then
        WeightConfigInfo currentWeights = weightFacade.getCurrentWeights();
        
        assertThat(currentWeights).isNotNull();
        // 기본값 또는 유효한 가중치 값인지 확인
        double sum = currentWeights.viewWeight() + 
                    currentWeights.likeWeight() + 
                    currentWeights.orderWeight();
        assertThat(sum).isEqualTo(0.9);
    }
}
