package com.loopers.infrastructure.ranking;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class RankingKeyGenerator {

    private static final String DAILY_RANKING_PREFIX = "ranking:all";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generateDailyRankingKey(LocalDate date) {
        return DAILY_RANKING_PREFIX + ":" + date.format(DATE_FORMATTER);
    }

}
