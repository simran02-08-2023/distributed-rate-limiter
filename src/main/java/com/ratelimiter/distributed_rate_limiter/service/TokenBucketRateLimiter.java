package com.ratelimiter.distributed_rate_limiter.service;

import com.ratelimiter.distributed_rate_limiter.config.RateLimiterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBucketRateLimiter implements RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> tokenBucketScript;
    private final RateLimiterProperties properties;

    @Override
    public boolean isAllowed(String clientId) {
        try {
            String key = "rate_limit:" + clientId;
            long now = System.currentTimeMillis();

            RateLimiterProperties.TierConfig config =
                    properties.getConfigForClient("default");

            Long result = redisTemplate.execute(
                    tokenBucketScript,
                    Collections.singletonList(key),
                    String.valueOf(config.getCapacity()),
                    String.valueOf(config.getRefillRate()),
                    String.valueOf(now)
            );

            boolean allowed = result != null && result == 1L;
            log.info("Client: {} | Allowed: {}", clientId, allowed);
            return allowed;

        } catch (Exception e) {
            log.error("Rate limiter error for client {}: {}", clientId, e.getMessage());
            return true;
        }
    }
}