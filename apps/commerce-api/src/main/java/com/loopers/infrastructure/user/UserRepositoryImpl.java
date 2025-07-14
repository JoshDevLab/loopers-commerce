package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Map<String, User> storage = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        storage.put(user.getUserId(), user);
        return user;
    }

    @Override
    public Optional<User> findByUserId(String userId) {
        return Optional.ofNullable(storage.get(userId));
    }

    @Override
    public boolean existByUserId(String userId) {
        return storage.containsKey(userId);
    }

    @Override
    public void deleteByUserId(String userId) {
        storage.remove(userId);
    }

}
