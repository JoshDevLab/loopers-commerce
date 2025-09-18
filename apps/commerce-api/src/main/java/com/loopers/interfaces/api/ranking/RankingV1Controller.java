package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingFacade;
import com.loopers.application.ranking.RankingInfo;
import com.loopers.application.ranking.ProductRankingInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.ranking.dto.RankingResponse;
import com.loopers.interfaces.api.ranking.dto.ProductRankingResponse;
import com.loopers.interfaces.api.ranking.dto.RankingType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/rankings")
public class RankingV1Controller {

    private final RankingFacade rankingFacade;

    @GetMapping
    public ApiResponse<?> getRankings(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStartDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date,
            @PageableDefault(size = 20, sort = "rank", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        if (type == null) {
            Page<RankingInfo> rankings = rankingFacade.getRankings(date, pageable);

            List<RankingResponse> responses = rankings.getContent().stream()
                    .map(info -> new RankingResponse(
                            info.rank(),
                            info.productId(),
                            info.productName(),
                            info.imageUrl(),
                            info.price(),
                            info.score()
                    ))
                    .toList();

            PageResponse<RankingResponse> pageResponse = new PageResponse<>(
                    responses,
                    rankings.getNumber(),
                    rankings.getSize(),
                    rankings.getTotalPages(),
                    rankings.getTotalElements()
            );

            return ApiResponse.success(pageResponse);
        }
        
        // type이 있으면 상품 랭킹 조회 (리스트)
        RankingType rankingType = RankingType.fromValue(type);
        List<ProductRankingInfo> rankings = getRankingsByType(rankingType, yearMonth, weekStartDate, date);
        
        List<ProductRankingResponse> responses = ProductRankingResponse.fromList(rankings);
                
        return ApiResponse.success(responses);
    }
    
    private List<ProductRankingInfo> getRankingsByType(RankingType type, String yearMonth, 
                                                      LocalDate weekStartDate, LocalDate date) {
        return switch (type) {
            case DAILY -> {
                if (date == null) {
                    throw new CoreException(ErrorType.BAD_REQUEST, "올바르지 않는 RankingType : " + type);
                }
                Page<RankingInfo> dailyRankings = rankingFacade.getRankings(date, Pageable.unpaged());
                yield dailyRankings.getContent().stream()
                        .map(rankingInfo -> ProductRankingInfo.fromDaily(rankingInfo, date))
                        .toList();
            }
            case WEEKLY -> {
                if (weekStartDate == null) {
                    throw new CoreException(ErrorType.BAD_REQUEST, "주간 랭킹조회는 StartDate 필수");
                }
                yield rankingFacade.getWeeklyRanking(weekStartDate);
            }
            case MONTHLY -> {
                if (yearMonth == null) {
                    throw new CoreException(ErrorType.BAD_REQUEST, "월간 랭킹조회는 Year-month 필수");
                }
                yield rankingFacade.getMonthlyRanking(yearMonth);
            }
        };
    }
}
