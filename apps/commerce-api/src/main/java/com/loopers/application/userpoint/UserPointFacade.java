package com.loopers.application.userpoint;

import com.loopers.domain.point.PointHistoryService;
import com.loopers.domain.point.PointInfo;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserPointFacade {

    private final UserService userService;
    private final PointService pointService;
    private final PointHistoryService pointHistoryService;

    @Transactional
    public UserInfo signUp(UserCommand.Register command) {
        UserInfo userInfo = userService.signUp(command);
        pointService.initPoint(userInfo.userId());
        return userInfo;
    }

    public PointInfo existMemberGetPoint(String userId) {
        return userService.existByUserId(userId) ? pointService.getPoint(userId) : null;
    }

    @Transactional
    public PointInfo existMemberChargingPoint(String userId, Long chargePoint) {
        if (!userService.existByUserId(userId))
            throw new CoreException(ErrorType.USER_NOT_FOUND, userId + "는 존재하지 않는 사용자입니다.");
        pointHistoryService.save(userId, chargePoint);
        return pointService.charge(userId, chargePoint);
    }

}
