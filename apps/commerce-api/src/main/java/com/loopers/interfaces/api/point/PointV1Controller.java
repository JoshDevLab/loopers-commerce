package com.loopers.interfaces.api.point;

import com.loopers.application.userpoint.UserPointFacade;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.User;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller {

    private final UserPointFacade userPointFacade;
    private final PointService pointService;


    @GetMapping
    public ApiResponse<Long> getPoints(@CurrentUser User user) {
        return ApiResponse.success(pointService.getPoint(user.getUserId()).getPointBalance());
    }
}
