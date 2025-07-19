package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findByUserId(String userId) {
        return userJpaRepository.findByUserId(userId);
    }

    @Override
    public boolean existByUserId(String userId) {
        return userJpaRepository.findByUserId(userId).isPresent();
    }

    @Override
    public void deleteByUserId(String userId) {
        Optional<User> user = userJpaRepository.findByUserId(userId);
        if (user.isPresent()) {
            userJpaRepository.delete(user.get());
        } else {
            throw new CoreException(
                    ErrorType.USER_NOT_FOUND,
                    userId + "는 존재하지 않는 유저입니다."
            );
        }
    }
}
