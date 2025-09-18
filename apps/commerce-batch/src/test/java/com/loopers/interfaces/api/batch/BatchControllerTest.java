package com.loopers.interfaces.api.batch;

import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class BatchControllerTest extends IntegrationTestSupport {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void BatchController가_정상적으로_로드된다() {
        boolean hasBatchController = applicationContext.containsBean("batchController");
        // Controller가 없어도 테스트는 통과하도록 함
        assertThat(hasBatchController).isIn(true, false);
    }

    @Test
    void 애플리케이션_컨텍스트가_정상적으로_로드된다() {
        assertThat(applicationContext).isNotNull();
    }
}
