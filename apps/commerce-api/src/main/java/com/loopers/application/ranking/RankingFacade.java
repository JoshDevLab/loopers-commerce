package com.loopers.application.ranking;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.RankingItem;
import com.loopers.domain.ranking.RankingService;
import com.loopers.domain.ranking.MvProductRankMonthly;
import com.loopers.domain.ranking.MvProductRankWeekly;
import com.loopers.domain.ranking.ProductRankMonthlyRepository;
import com.loopers.domain.ranking.ProductRankWeeklyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RankingFacade {

    private final RankingService rankingService;
    private final ProductService productService;
    private final ProductRankMonthlyRepository monthlyRepository;
    private final ProductRankWeeklyRepository weeklyRepository;

    public Page<RankingInfo> getRankings(LocalDate date, Pageable pageable) {
        if (date == null) {
            date = LocalDate.now();
        }

        List<RankingItem> rankingItems = rankingService.getRankings(date, pageable.getOffset(), pageable.getPageSize());

        if (rankingItems.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> productIds = rankingItems.stream()
                .map(RankingItem::getProductId)
                .toList();

        List<Product> products = productService.getProductsByIds(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<RankingInfo> rankingInfos = rankingItems.stream()
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    return new RankingInfo(
                            item.getRank(),
                            item.getProductId(),
                            product != null ? product.getName() : "상품명 없음",
                            product != null ? product.getImageUrl() : null,
                            product != null ? product.getBasicPrice() : null,
                            item.getScore()
                    );
                })
                .toList();

        long totalCount = rankingService.getTotalCount(date);

        return new PageImpl<>(rankingInfos, pageable, totalCount);
    }

    // 월간 랭킹 조회
    public List<ProductRankingInfo> getMonthlyRanking(String reportMonth) {
        return monthlyRepository.findTop10ByReportMonthOrderByRankPosition(reportMonth)
                .stream()
                .map(ProductRankingInfo::fromMonthly)
                .toList();
    }
    
    // 주간 랭킹 조회
    public List<ProductRankingInfo> getWeeklyRanking(LocalDate weekStartDate) {
        return weeklyRepository.findTop10ByWeekStartDateOrderByRankPosition(weekStartDate)
                .stream()
                .map(ProductRankingInfo::fromWeekly)
                .toList();
    }
}
