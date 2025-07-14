package com.loopers.interfaces.api.user;

import com.loopers.application.userpoint.UserPointFacade;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRegisterCommand;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.dto.MyInfoResponse;
import com.loopers.interfaces.api.user.dto.SignUpRequest;
import com.loopers.interfaces.api.user.dto.SignUpResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {

    private final UserService userService;
    private final UserPointFacade userPointFacade;

    @PostMapping
    public ApiResponse<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        UserRegisterCommand command = request.toCommand();
        User user = userPointFacade.signUp(command);
        return ApiResponse.success(SignUpResponse.from(user));
    }

    @GetMapping("/me")
    public ApiResponse<MyInfoResponse> getMyInfo(@CurrentUserId String userId) {
        User myInfo = userService.getMyInfo(userId);

        if (myInfo == null) {
            throw new CoreException(ErrorType.USER_NOT_FOUND, userId + "는 존재하지 않는 유저입니다.");
        }

        return ApiResponse.success(MyInfoResponse.from(myInfo));
    }
}
