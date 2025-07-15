package com.loopers.interfaces.api.point;

import com.loopers.application.userpoint.UserPointFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller {

    private final UserPointFacade userPointFacade;

    @GetMapping
    public ApiResponse<Long> getPoints(@CurrentUserId String userId) {
        return ApiResponse.success(userPointFacade.existMemberGetPoint(userId).getPointBalance());
    }
}
