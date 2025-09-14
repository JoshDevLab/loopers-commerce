package com.loopers.application.ranking;

import com.loopers.domain.ranking.WeightConfigInfo;
import com.loopers.domain.ranking.WeightConfigService;
import com.loopers.domain.ranking.WeightResetCommand;
import com.loopers.domain.ranking.WeightUpdateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeightFacade 테스트")
class WeightFacadeTest {

    @Mock
    WeightConfigService weightConfigService;

    @InjectMocks
    WeightFacade sut;

    @Test
    @DisplayName("가중치 업데이트 요청 시 WeightConfigService를 호출한다")
    void updateWeights() {
        // given
        WeightUpdateCommand command = new WeightUpdateCommand(0.1, 0.3, 0.6);

        // when
        sut.updateWeights(command);

        // then
        verify(weightConfigService, times(1))
                .updateWeights(0.1, 0.3, 0.6);
    }

    @Test
    @DisplayName("현재 가중치 조회 시 WeightConfigService에서 데이터를 가져온다")
    void getCurrentWeights() {
        // given
        WeightConfigInfo expectedInfo = new WeightConfigInfo(0.1, 0.2, 0.7);
        when(weightConfigService.getCurrentWeights()).thenReturn(expectedInfo);

        // when
        WeightConfigInfo result = sut.getCurrentWeights();

        // then
        assertThat(result).isSameAs(expectedInfo);
        verify(weightConfigService, times(1)).getCurrentWeights();
    }

    @Test
    @DisplayName("가중치 초기화 요청 시 WeightConfigService를 호출한다")
    void resetWeights() {
        // given
        WeightResetCommand command = new WeightResetCommand("관리자 초기화");

        // when
        sut.resetWeights(command);

        // then
        verify(weightConfigService, times(1))
                .resetToDefault("관리자 초기화");
    }
}
