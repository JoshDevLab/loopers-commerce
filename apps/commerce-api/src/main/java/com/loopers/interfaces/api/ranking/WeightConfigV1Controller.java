package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.WeightFacade;
import com.loopers.domain.ranking.WeightConfigInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.ranking.dto.WeightConfigResponse;
import com.loopers.interfaces.api.ranking.dto.WeightResetRequest;
import com.loopers.interfaces.api.ranking.dto.WeightUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin")
public class WeightConfigV1Controller {
    private final WeightFacade weightFacade;

    @GetMapping("/ranking/weights")
    public ApiResponse<WeightConfigResponse> getCurrentWeights() {
        WeightConfigInfo currentWeights = weightFacade.getCurrentWeights();
        WeightConfigResponse response = WeightConfigResponse.from(currentWeights);

        return ApiResponse.success(response);
    }

    @PutMapping("/ranking/weights")
    public ApiResponse<Object> updateWeights(@Valid @RequestBody WeightUpdateRequest request) {
        weightFacade.updateWeights(request.toCommand());

        return ApiResponse.success();
    }

    @PostMapping("/ranking/weights/reset")
    public ApiResponse<Object> resetWeights(@RequestBody(required = false) WeightResetRequest request) {
        WeightResetRequest resetRequest = request != null ? request : new WeightResetRequest("관리자 초기화");
        weightFacade.resetWeights(resetRequest.toCommand());

        return ApiResponse.success();
    }
}
