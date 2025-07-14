package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findByUserId(String userId);

    boolean existByUserId(String userId);

    void deleteByUserId(String userId);
}
