package com.loopers.support.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisDistributedLock {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;

    private static final String UNLOCK_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";


    public boolean executeWithLock(String lockKey, Duration lockTimeout, Runnable task) {
        return executeWithLock(lockKey, lockTimeout, () -> {
            task.run();
            return true;
        });
    }

    public <T> T executeWithLock(String lockKey, Duration lockTimeout, Supplier<T> task) {
        String lockValue = UUID.randomUUID().toString();

        try {
            // 락 획득 시도
            boolean lockAcquired = tryLock(lockKey, lockValue, lockTimeout);

            if (!lockAcquired) {
                log.debug("분산 락 획득 실패: {}", lockKey);
                return null;
            }

            log.debug("분산 락 획득 성공: {}, value: {}", lockKey, lockValue);

            // 작업 실행
            return task.get();

        } catch (Exception e) {
            log.error("분산 락 작업 실행 중 오류 발생: {}", lockKey, e);
            throw e;
        } finally {
            // 락 해제
            releaseLock(lockKey, lockValue);
        }
    }

    private boolean tryLock(String lockKey, String lockValue, Duration timeout) {
        try {
            // Redis SET 명령의 NX 옵션: key가 존재하지 않을 때만 설정
            // EX 옵션: 만료 시간 설정 (초 단위)
            String result = Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(
                    lockKey,
                    lockValue,
                    timeout
            )) ? LOCK_SUCCESS : null;

            return LOCK_SUCCESS.equals(result) || Boolean.TRUE.equals(
                    stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeout)
            );

        } catch (Exception e) {
            log.error("락 획득 시도 중 오류: {}", lockKey, e);
            return false;
        }
    }

    private void releaseLock(String lockKey, String lockValue) {
        try {
            RedisScript<Long> redisScript = RedisScript.of(UNLOCK_LUA_SCRIPT, Long.class);
            Long result = stringRedisTemplate.execute(
                    redisScript,
                    Collections.singletonList(lockKey),
                    lockValue
            );

            if (RELEASE_SUCCESS.equals(result)) {
                log.debug("분산 락 해제 성공: {}", lockKey);
            } else {
                log.warn("분산 락 해제 실패 (이미 만료되었거나 다른 프로세스가 소유): {}", lockKey);
            }

        } catch (Exception e) {
            log.error("락 해제 중 오류: {}", lockKey, e);
        }
    }
}
