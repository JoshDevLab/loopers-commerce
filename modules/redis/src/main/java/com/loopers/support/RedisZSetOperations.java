package com.loopers.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisZSetOperations {

    private final RedisTemplate<String, String> redisTemplate;

    public Double incrementScore(String key, String member, double delta) {
        try {
            return redisTemplate.opsForZSet().incrementScore(key, member, delta);
        } catch (Exception e) {
            log.error("ZSET 점수 증가 실패: key={}, member={}, delta={}", key, member, delta, e);
            throw new RedisOperationException("ZSET 점수 증가 실패", e);
        }
    }

    public Boolean add(String key, String member, double score) {
        try {
            return redisTemplate.opsForZSet().add(key, member, score);
        } catch (Exception e) {
            log.error("ZSET 멤버 추가 실패: key={}, member={}, score={}", key, member, score, e);
            throw new RedisOperationException("ZSET 멤버 추가 실패", e);
        }
    }

    public Set<ZSetOperations.TypedTuple<String>> reverseRangeWithScores(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        } catch (Exception e) {
            log.error("ZSET 역순 범위 조회 실패: key={}, start={}, end={}", key, start, end, e);
            throw new RedisOperationException("ZSET 범위 조회 실패", e);
        }
    }

    public Set<String> reverseRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().reverseRange(key, start, end);
        } catch (Exception e) {
            log.error("ZSET 역순 범위 조회 실패: key={}, start={}, end={}", key, start, end, e);
            throw new RedisOperationException("ZSET 범위 조회 실패", e);
        }
    }

    public Long reverseRank(String key, String member) {
        try {
            return redisTemplate.opsForZSet().reverseRank(key, member);
        } catch (Exception e) {
            log.error("ZSET 역순 순위 조회 실패: key={}, member={}", key, member, e);
            return null;
        }
    }

    public Double score(String key, String member) {
        try {
            return redisTemplate.opsForZSet().score(key, member);
        } catch (Exception e) {
            log.error("ZSET 점수 조회 실패: key={}, member={}", key, member, e);
            return null;
        }
    }

    public Long size(String key) {
        try {
            return redisTemplate.opsForZSet().size(key);
        } catch (Exception e) {
            log.error("ZSET 크기 조회 실패: key={}", key, e);
            return 0L;
        }
    }

    public Long zCard(String key) {
        try {
            return redisTemplate.opsForZSet().zCard(key);
        } catch (Exception e) {
            log.error("ZSET 카드 수 조회 실패: key={}", key, e);
            return 0L;
        }
    }

    public Boolean expire(String key, Duration timeout) {
        try {
            return redisTemplate.expire(key, timeout);
        } catch (Exception e) {
            log.error("TTL 설정 실패: key={}, timeout={}", key, timeout, e);
            return false;
        }
    }

    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("키 존재 확인 실패: key={}", key, e);
            return false;
        }
    }

    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("키 삭제 실패: key={}", key, e);
            return false;
        }
    }

    public static class RedisOperationException extends RuntimeException {
        public RedisOperationException(String message, Exception e) {
            super(message, e);
        }
    }
}
