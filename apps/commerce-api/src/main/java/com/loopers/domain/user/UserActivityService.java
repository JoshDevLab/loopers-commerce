package com.loopers.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserActivityService {
    private final UserActivityRepository userActivityRepository;

    @Transactional
    public void save(UserActivityCommand command) {
        userActivityRepository.save(UserActivity.create(command));
    }
}
