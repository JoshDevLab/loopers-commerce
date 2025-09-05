package com.loopers.domain.stockadjusted;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StockAdjustedService {
    private final StringRedisTemplate stringRedisTemplate;

    public void StockAdjusted(StockAdjustedCommand command) {
        String cacheKey = "product:option:v1:" + command.getProductOptionId();
        stringRedisTemplate.delete(cacheKey);
    }
}
