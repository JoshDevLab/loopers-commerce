package com.loopers.interfaces.api.point;

import com.loopers.application.userpoint.UserPointFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller {

    private final UserPointFacade userPointFacade;

    @GetMapping
    public ApiResponse<Long> getPoints(@CurrentUserId String userId) {
        return ApiResponse.success(userPointFacade.existMemberGetPoint(userId).pointBalance());
    }

    @PostMapping("/charge")
    public ApiResponse<Long> chargePoints(@CurrentUserId String userId, @RequestBody Long chargePoint) {
        return ApiResponse.success(userPointFacade.existMemberChargingPoint(userId, chargePoint).pointBalance());
    }
}
