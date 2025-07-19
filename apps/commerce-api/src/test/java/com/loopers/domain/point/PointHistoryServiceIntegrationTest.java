package com.loopers.domain.point;

import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class PointHistoryServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private PointHistoryService pointHistoryService;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private PointRepository pointRepository;

    @DisplayName("포인트 충전 이력이 저장되고 ID가 반환된다")
    @Test
    void save_success() {
        // given
        String userId = "test123";
        Long point = 500L;

        // when
        Long savedId = pointHistoryRepository.save(PointHistory.create(userId, point, PointHistoryType.CHARGE));

        // then
        assertThat(savedId).isNotNull();
        PointHistory found = pointHistoryRepository.findById(savedId);
        assertThat(found).isNotNull();
        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getPoint()).isEqualTo(point);
        assertThat(found.getType()).isEqualTo(PointHistoryType.CHARGE);
    }

    @DisplayName("포인트 충전 이력을 삭제하면 제거된다")
    @Test
    void deletePointHistory_success() {
        // given
        String userId = "test123";
        Long point = 300L;

        Long savedId = pointHistoryRepository.save(PointHistory.create(userId, point, PointHistoryType.CHARGE));

        // when
        pointHistoryService.delete(savedId);

        // then
        assertThat(savedId).isNotNull();
        PointHistory found = pointHistoryRepository.findById(savedId);
        assertThat(found).isNull();
    }
}
