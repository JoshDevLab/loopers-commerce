package com.loopers.batch.job;

import com.loopers.batch.dto.ProductMetricsAggregation;
import com.loopers.batch.dto.ProductRankingData;
import com.loopers.domain.ranking.WeightConfigInfo;
import com.loopers.domain.ranking.WeightConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeeklyRankingJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final WeightConfigService weightConfigService;
    
    private static final int CHUNK_SIZE = 1000;
    
    @Bean
    public Job weeklyRankingJob() {
        return new JobBuilder("weeklyRankingJob", jobRepository)
                .start(clearWeeklyRankingStep())
                .next(aggregateWeeklyRankingStep())
                .build();
    }
    
    @Bean
    public Step clearWeeklyRankingStep() {
        return new StepBuilder("clearWeeklyRankingStep", jobRepository)
                .tasklet(clearWeeklyRankingTasklet(), transactionManager)
                .build();
    }
    
    @Bean
    public Step aggregateWeeklyRankingStep() {
        return new StepBuilder("aggregateWeeklyRankingStep", jobRepository)
                .<ProductMetricsAggregation, ProductRankingData>chunk(CHUNK_SIZE, transactionManager)
                .reader(weeklyRankingReader(null))
                .processor(weeklyRankingProcessor())
                .writer(weeklyRankingWriter())
                .build();
    }
    
    @Bean
    public Tasklet clearWeeklyRankingTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate now = LocalDate.now();
            LocalDate weekStart = now.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);
            
            log.info("주간 랭킹 데이터 삭제 시작: {} ~ {}", weekStart, weekEnd);
            
            String deleteSql = """
                DELETE FROM mv_product_rank_weekly
                WHERE week_start_date = ? AND week_end_date = ?
                """;
            
            int deletedCount = jdbcTemplate.update(deleteSql, weekStart, weekEnd);
            log.info("기존 주간 랭킹 데이터 {} 건 삭제", deletedCount);
            
            return RepeatStatus.FINISHED;
        };
    }
    
    @Bean
    @StepScope
    public ItemReader<ProductMetricsAggregation> weeklyRankingReader(
            @Value("#{jobParameters['weekStartDate'] ?: T(java.time.LocalDate).now().minusWeeks(1).with(T(java.time.DayOfWeek).MONDAY).toString()}") 
            String weekStartDate) {
        
        // 기존 WeightConfigService에서 가중치 조회
        WeightConfigInfo weightConfig = weightConfigService.getCurrentWeights();
        
        LocalDate startDate = LocalDate.parse(weekStartDate);
        LocalDate endDate = startDate.plusDays(6);

        log.info("주간 랭킹 데이터 읽기 시작: {} ~ {}", startDate, endDate);
        log.info("적용된 가중치 - 조회:{}, 좋아요:{}, 주문:{}", 
                weightConfig.viewWeight(), weightConfig.likeWeight(), weightConfig.orderWeight());

        // 기존 시스템의 가중치를 사용한 동적 SQL
        // orderWeight를 sales_count에 매핑
        String sql = String.format("""
            SELECT
                pm.product_id,
                p.name as product_name,
                SUM(pm.sales_count) as total_sales,
                SUM(pm.view_count) as total_views,
                SUM(pm.like_count) as total_likes,
                (SUM(pm.view_count) * %.3f + SUM(pm.like_count) * %.3f + SUM(pm.sales_count) * %.3f) as total_score
            FROM product_metrics pm
            JOIN product p ON pm.product_id = p.id
            WHERE DATE(pm.metric_date) BETWEEN ? AND ?
              AND p.deleted_at IS NULL
            GROUP BY pm.product_id, p.name
            HAVING total_score > 0
            ORDER BY total_score DESC
            LIMIT 100
            """, weightConfig.viewWeight(), weightConfig.likeWeight(), weightConfig.orderWeight());
        
        return new JdbcCursorItemReaderBuilder<ProductMetricsAggregation>()
                .name("weeklyRankingReader")
                .dataSource(dataSource)
                .sql(sql)
                .preparedStatementSetter(ps -> {
                    ps.setDate(1, java.sql.Date.valueOf(startDate));
                    ps.setDate(2, java.sql.Date.valueOf(endDate));
                })
                .rowMapper(new ProductMetricsRowMapper())
                .build();
    }
    
    @Bean
    public ItemProcessor<ProductMetricsAggregation, ProductRankingData> weeklyRankingProcessor() {
        AtomicInteger rankPosition = new AtomicInteger(1);
        
        return item -> {
            LocalDate now = LocalDate.now();
            LocalDate weekStart = now.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);
            ZonedDateTime currentTime = ZonedDateTime.now();
            
            log.debug("처리 중인 상품: {} (점수: {})", item.getProductName(), item.getTotalScore());
            
            return ProductRankingData.weeklyBuilder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .weekStartDate(weekStart)
                    .weekEndDate(weekEnd)
                    .totalSales(item.getTotalSales())
                    .totalViews(item.getTotalViews())
                    .totalLikes(item.getTotalLikes())
                    .totalScore(item.getTotalScore())
                    .rankPosition(rankPosition.getAndIncrement())
                    .createdAt(currentTime)
                    .updatedAt(currentTime)
                    .build();
        };
    }
    
    @Bean
    public ItemWriter<ProductRankingData> weeklyRankingWriter() {
        String sql = """
            INSERT INTO mv_product_rank_weekly (
                product_id, product_name, week_start_date, week_end_date, 
                total_sales, total_views, total_likes, total_score, rank_position, 
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        return new JdbcBatchItemWriterBuilder<ProductRankingData>()
                .dataSource(dataSource)
                .sql(sql)
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, item.getProductId());
                    ps.setString(2, item.getProductName());
                    ps.setDate(3, java.sql.Date.valueOf(item.getWeekStartDate()));
                    ps.setDate(4, java.sql.Date.valueOf(item.getWeekEndDate()));
                    ps.setLong(5, item.getTotalSales());
                    ps.setLong(6, item.getTotalViews());
                    ps.setLong(7, item.getTotalLikes());
                    ps.setDouble(8, item.getTotalScore());
                    ps.setInt(9, item.getRankPosition());
                    ps.setTimestamp(10, java.sql.Timestamp.from(item.getCreatedAt().toInstant()));
                    ps.setTimestamp(11, java.sql.Timestamp.from(item.getUpdatedAt().toInstant()));
                })
                .build();
    }
    
    private static class ProductMetricsRowMapper implements RowMapper<ProductMetricsAggregation> {
        @Override
        public ProductMetricsAggregation mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ProductMetricsAggregation.builder()
                    .productId(rs.getLong("product_id"))
                    .productName(rs.getString("product_name"))
                    .totalSales(rs.getLong("total_sales"))
                    .totalViews(rs.getLong("total_views"))
                    .totalLikes(rs.getLong("total_likes"))
                    .totalScore(rs.getDouble("total_score"))
                    .build();
        }
    }
}
