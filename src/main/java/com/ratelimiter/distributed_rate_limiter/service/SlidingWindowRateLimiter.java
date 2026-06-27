package com.ratelimiter.distributed_rate_limiter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlidingWindowRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    private static final long WINDOW_SIZE_SECONDS = 60;
    private static final long MAX_REQUESTS = 10;

    public boolean isAllowed(String clientId) {
        try {
            String key = "sliding_window:" + clientId;
            long now = System.currentTimeMillis();
            long windowStart = now - (WINDOW_SIZE_SECONDS * 1000);

            // Remove requests older than the window
            redisTemplate.opsForZSet().removeRangeByScore(
                    key, 0, windowStart
            );

            // Count requests in current window
            Long requestCount = redisTemplate.opsForZSet().zCard(key);

            if (requestCount == null || requestCount < MAX_REQUESTS) {
                // Add current request with timestamp as score
                redisTemplate.opsForZSet().add(
                        key,
                        String.valueOf(now),
                        now
                );
                redisTemplate.expire(key, WINDOW_SIZE_SECONDS, TimeUnit.SECONDS);

                log.info("Sliding window | Client: {} | Count: {}/{}",
                        clientId, requestCount == null ? 1 : requestCount + 1, MAX_REQUESTS);
                return true;
            }

            log.warn("Sliding window blocked | Client: {} | Count: {}/{}",
                    clientId, requestCount, MAX_REQUESTS);
            return false;

        } catch (Exception e) {
            log.error("Sliding window error: {}", e.getMessage());
            return true;
        }
    }
}