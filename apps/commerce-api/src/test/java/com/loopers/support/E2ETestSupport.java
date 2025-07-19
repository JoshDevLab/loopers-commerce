package com.loopers.support;

import com.loopers.utils.DatabaseCleanUp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class E2ETestSupport {
    @Autowired
    protected TestRestTemplate client;

    @Autowired
    protected DatabaseCleanUp databaseCleanUp;

}
