package com.loopers.domain.user;

import java.util.List;

public interface UserActivityRepository {
    void save(UserActivity userActivity);

    List<UserActivity> findAll();
}
