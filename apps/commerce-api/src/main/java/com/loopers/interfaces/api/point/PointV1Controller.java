package com.loopers.interfaces.api.point;

import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller {

    private final PointService pointService;

    @GetMapping
    public ApiResponse<Long> getPoints(@CurrentUser UserInfo userInfo) {
        return ApiResponse.success(pointService.getPoint(userInfo.id()).pointBalance());
    }

    @PostMapping("/charge")
    public ApiResponse<Long> chargePoints(@CurrentUser UserInfo userInfo, @RequestBody Long chargePoint) {
        return ApiResponse.success(pointService.charge(userInfo.id(), chargePoint).pointBalance());
    }
}
