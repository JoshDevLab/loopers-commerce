package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingFacade;
import com.loopers.application.ranking.RankingInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.ranking.dto.RankingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/rankings")
public class RankingV1Controller {

    private final RankingFacade rankingFacade;

    @GetMapping
    public ApiResponse<PageResponse<RankingResponse>> getRankings(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date,
            @PageableDefault(size = 20, sort = "rank", direction = Sort.Direction.ASC) Pageable pageable
    ) {
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
}
