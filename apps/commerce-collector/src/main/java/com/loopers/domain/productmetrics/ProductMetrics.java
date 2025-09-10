package com.loopers.domain.productmetrics;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "product_metrics",
        indexes = {
                @Index(name = "idx_product_metrics_product_date", columnList = "product_id, metric_date"),
                @Index(name = "idx_product_metrics_date", columnList = "metric_date"),
                @Index(name = "idx_product_metrics_like_count", columnList = "like_count DESC")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_metrics_product_date", columnNames = {"product_id", "metric_date"})
        }
)
public class ProductMetrics extends BaseEntity {
    private Long productId;
    private ZonedDateTime metricDate;
    private Integer likeCount = 0;
    private Integer viewCount = 0;
    private Integer salesCount = 0;

    @Builder
    private ProductMetrics(Long productId, ZonedDateTime metricDate, Integer likeCount, Integer viewCount, Integer salesCount) {
        this.productId = productId;
        this.metricDate = metricDate != null ? metricDate : ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault());
        this.likeCount = likeCount != null ? likeCount : 0;
        this.viewCount = viewCount != null ? viewCount : 0;
        this.salesCount = salesCount != null ? salesCount : 0;
    }

    public static ProductMetrics createNew(Long productId, ZonedDateTime date) {
        return ProductMetrics.builder()
                .productId(productId)
                .metricDate(date != null ? date : ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()))
                .build();
    }

    public static ProductMetrics createToday(Long productId) {
        return createNew(productId, ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()));
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementSalesCount() {
        this.salesCount++;
    }

}
