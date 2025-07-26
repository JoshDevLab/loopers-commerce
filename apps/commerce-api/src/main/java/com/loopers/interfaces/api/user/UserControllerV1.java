package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.domain.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.dto.MyInfoResponse;
import com.loopers.interfaces.api.user.dto.SignUpRequest;
import com.loopers.interfaces.api.user.dto.SignUpResponse;
import com.loopers.interfaces.interceptor.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {

    private final UserFacade userFacade;

    @PostMapping
    public ApiResponse<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        return ApiResponse.success(SignUpResponse.from(userFacade.signUp(request.toCommand())));
    }

    @GetMapping("/me")
    public ApiResponse<MyInfoResponse> getMyInfo(@CurrentUser UserInfo userInfo) {
        return ApiResponse.success(MyInfoResponse.from(userInfo));
    }
}
