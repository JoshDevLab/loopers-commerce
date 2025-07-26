package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointFacade;
import com.loopers.domain.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.interceptor.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller {

    private final PointFacade pointFacade;

    @GetMapping
    public ApiResponse<BigDecimal> getPoints(@CurrentUser UserInfo userInfo) {
        return ApiResponse.success(pointFacade.getPoint(userInfo.id()).pointBalance());
    }

    @PostMapping("/charge")
    public ApiResponse<BigDecimal> chargePoints(@CurrentUser UserInfo userInfo, @RequestBody BigDecimal chargePoint) {
        return ApiResponse.success(pointFacade.charge(userInfo.id(), chargePoint).pointBalance());
    }
}
