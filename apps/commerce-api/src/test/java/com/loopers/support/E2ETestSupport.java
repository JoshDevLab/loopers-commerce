package com.loopers.support;

import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class E2ETestSupport {
    @Autowired
    protected TestRestTemplate client;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @BeforeEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

}
