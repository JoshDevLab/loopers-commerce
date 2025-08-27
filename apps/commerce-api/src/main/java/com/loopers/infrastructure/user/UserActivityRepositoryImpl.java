package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserActivity;
import com.loopers.domain.user.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserActivityRepositoryImpl implements UserActivityRepository {
    private final UserActivityJpaRepository userActivityJpaRepository;

    @Override
    public void save(UserActivity userActivity) {
        userActivityJpaRepository.save(userActivity);
    }
}
