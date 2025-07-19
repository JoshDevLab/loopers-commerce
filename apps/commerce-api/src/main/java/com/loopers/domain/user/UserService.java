package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserInfo signUp(UserCommand.Register command) {
        if (userRepository.existByUserId(command.userId())) {
            throw new CoreException(ErrorType.ALREADY_EXIST_USERID, command.userId() + "는 이미 존재하는 아이디입니다.");
        }

        User user = User.create(
                command.userId(),
                command.email(),
                command.birthday(),
                command.gender()
        );

        return UserInfo.of(userRepository.save(user));
    }

    public UserInfo getMyInfo(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() ->
                new CoreException(ErrorType.USER_NOT_FOUND, userId + "는 존재하지 않는 유저입니다."));
        return UserInfo.of(user);
    }

    public boolean existByUserId(String userId) {
        return userRepository.existByUserId(userId);
    }

    public void deleteUser(String userId) {
        userRepository.deleteByUserId(userId);
    }
}
