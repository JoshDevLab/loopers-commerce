package com.loopers.application.userpoint;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRegisterCommand;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserPointFacade {

    private final UserService userService;
    private final PointService pointService;

    public User signUp(UserRegisterCommand command) {
        User user = userService.signUp(command);

        try {
            pointService.initPoint(user.getUserId());
        } catch (Exception e) {
            userService.deleteUser(user.getUserId());
            throw new CoreException(ErrorType.INTERNAL_ERROR, "회원 가입에 실패하였습니다. 다시 시도해주세요");
        }

        return user;
    }

    public Point existMemberGetPoint(String userId) {
        return userService.existByUserId(userId) ? pointService.getPoint(userId) : null;
    }
}
