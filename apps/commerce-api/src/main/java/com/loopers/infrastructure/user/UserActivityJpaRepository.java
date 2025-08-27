package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityJpaRepository extends JpaRepository<UserActivity, Long> {
}
