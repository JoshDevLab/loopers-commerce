package com.loopers.infrastructure.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RankingKeyGeneratorTest {

    RankingKeyGenerator sut = new RankingKeyGenerator();

    @DisplayName("일일 랭킹 키를 올바른 형식으로 생성한다")
    @Test
    void generateDailyRankingKey() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 1, 15);

        // Act
        String result = sut.generateDailyRankingKey(date);

        // Assert
        assertThat(result).isEqualTo("ranking:all:20250115");
    }

    @DisplayName("오늘 날짜로 일일 랭킹 키를 생성한다")
    @Test
    void generateDailyRankingKey_today() {
        // Arrange
        LocalDate today = LocalDate.now();

        // Act
        String result = sut.generateDailyRankingKey(today);

        // Assert
        assertThat(result).startsWith("ranking:all:");
        assertThat(result).contains(today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    @DisplayName("서로 다른 날짜는 서로 다른 키를 생성한다")
    @Test
    void generateDailyRankingKey_differentDates() {
        // Arrange
        LocalDate date1 = LocalDate.of(2025, 1, 15);
        LocalDate date2 = LocalDate.of(2025, 1, 16);

        // Act
        String key1 = sut.generateDailyRankingKey(date1);
        String key2 = sut.generateDailyRankingKey(date2);

        // Assert
        assertThat(key1).isNotEqualTo(key2);
        assertThat(key1).isEqualTo("ranking:all:20250115");
        assertThat(key2).isEqualTo("ranking:all:20250116");
    }

    @DisplayName("년말년시 경계에서도 올바른 키를 생성한다")
    @Test
    void generateDailyRankingKey_yearBoundary() {
        // Arrange
        LocalDate lastDayOf2024 = LocalDate.of(2024, 12, 31);
        LocalDate firstDayOf2025 = LocalDate.of(2025, 1, 1);

        // Act
        String key2024 = sut.generateDailyRankingKey(lastDayOf2024);
        String key2025 = sut.generateDailyRankingKey(firstDayOf2025);

        // Assert
        assertThat(key2024).isEqualTo("ranking:all:20241231");
        assertThat(key2025).isEqualTo("ranking:all:20250101");
        assertThat(key2024).isNotEqualTo(key2025);
    }
}
