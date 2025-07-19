package com.loopers.application.userpoint;

import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserPointFacade {

    private final UserService userService;
    private final PointService pointService;

    @Transactional
    public UserInfo signUp(UserCommand.Register command) {
        UserInfo userInfo = userService.signUp(command);
        pointService.initPoint(userInfo.userId());
        return userInfo;
    }

}
