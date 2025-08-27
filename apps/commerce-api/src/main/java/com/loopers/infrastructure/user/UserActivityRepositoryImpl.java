package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserActivity;
import com.loopers.domain.user.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class UserActivityRepositoryImpl implements UserActivityRepository {
    private final UserActivityJpaRepository userActivityJpaRepository;

    @Override
    public void save(UserActivity userActivity) {
        userActivityJpaRepository.save(userActivity);
    }

    @Override
    public List<UserActivity> findAll() {
        return userActivityJpaRepository.findAll();
    }
}
