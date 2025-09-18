package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "mv_product_rank_monthly")
public class MvProductRankMonthly extends BaseEntity {
    private Long productId;
    private String productName;

    @Column(name = "report_month", length = 7)
    private String reportMonth; // "2024-01" 형태

    private Long totalSales;
    private Long totalViews;
    private Long totalLikes;
    private Double totalScore;
    private Integer rankPosition;
}
