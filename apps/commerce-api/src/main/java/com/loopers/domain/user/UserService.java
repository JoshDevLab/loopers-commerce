package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User signUp(UserCommand.Register command) {
        if (userRepository.existByUserId(command.userId())) {
            throw new CoreException(ErrorType.ALREADY_EXIST_USERID, command.userId() + "는 이미 존재하는 아이디입니다.");
        }

        User user = User.create(
                command.userId(),
                command.email(),
                command.birthday(),
                command.gender()
        );

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getMyInfoByUserId(String userId) {
        return userRepository.findByUserId(userId).orElseThrow(() ->
                new CoreException(ErrorType.USER_NOT_FOUND, userId + "는 존재하지 않는 유저입니다."));
    }

    @Transactional(readOnly = true)
    public User getMyInfoByUserPk(Long userPk) {
        return userRepository.findById(userPk).orElseThrow(() ->
                new CoreException(ErrorType.USER_NOT_FOUND, "존재하지 않는 유저입니다."));
    }

}
