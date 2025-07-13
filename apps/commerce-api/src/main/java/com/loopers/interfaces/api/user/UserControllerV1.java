package com.loopers.interfaces.api.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRegisterCommand;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.dto.SignUpRequest;
import com.loopers.interfaces.api.user.dto.SignUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {

    private final UserService userService;

    @PostMapping
    public ApiResponse<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        UserRegisterCommand command = request.toCommand();
        User user = userService.signUp(command);
        return ApiResponse.success(SignUpResponse.from(user));
    }
}
