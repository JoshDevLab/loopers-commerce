package com.loopers.support;

import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public abstract class IntegrationTestSupport {

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @BeforeEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

}
