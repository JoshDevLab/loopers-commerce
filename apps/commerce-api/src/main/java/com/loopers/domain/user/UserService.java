package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public User signUp(UserRegisterCommand command) {
        if (userRepository.existByUserId(command.getUserId())) {
            throw new CoreException(ErrorType.ALREADY_EXIST_USERID, command.getUserId() + "는 이미 존재하는 아이디입니다.");
        }

        User user = User.create(
                command.getUserId(),
                command.getEmail(),
                command.getBirthday(),
                command.getGender()
        );

        return userRepository.save(user);
    }

    public User getMyInfo(String userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }

    public boolean existByUserId(String userId) {
        return userRepository.existByUserId(userId);
    }

    public void deleteUser(String userId) {
        userRepository.deleteByUserId(userId);
    }
}
