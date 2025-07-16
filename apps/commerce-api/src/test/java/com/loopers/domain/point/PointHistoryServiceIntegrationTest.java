package com.loopers.domain.point;

import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static com.loopers.support.InMemoryDbSupport.clearInMemoryStorage;
import static org.assertj.core.api.Assertions.assertThat;

public class PointHistoryServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private PointHistoryService pointHistoryService;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private PointRepository pointRepository;

    @BeforeEach
    void reset() throws Exception {
        clearInMemoryStorage(pointHistoryRepository);
        clearInMemoryStorage(pointRepository);
    }

    @DisplayName("포인트 충전 이력이 저장되고 ID가 반환된다")
    @Test
    void chargePointHistory_success() {
        // given
        String userId = "test123";
        Long point = 500L;
        LocalDateTime registeredAt = LocalDateTime.now();

        // when
        Long savedId = pointHistoryRepository.save(PointHistory.create(userId, point, PointHistoryType.CHARGE, registeredAt));

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
        LocalDateTime registeredAt = LocalDateTime.now();

        Long savedId = pointHistoryRepository.save(PointHistory.create(userId, point, PointHistoryType.CHARGE, registeredAt));

        // when
        pointHistoryService.delete(savedId);

        // then
        assertThat(savedId).isNotNull();
        PointHistory found = pointHistoryRepository.findById(savedId);
        assertThat(found).isNull();
    }
}
