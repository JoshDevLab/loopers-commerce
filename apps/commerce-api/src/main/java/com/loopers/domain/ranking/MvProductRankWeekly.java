package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static lombok.AccessLevel.*;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
@Table(name = "mv_product_rank_weekly")
public class MvProductRankWeekly extends BaseEntity {
    private Long productId;
    private String productName;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private Long totalSales;
    private Long totalViews;
    private Long totalLikes;
    private Double totalScore;
    private Integer rankPosition;
}
