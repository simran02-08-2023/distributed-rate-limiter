package com.ratelimiter.distributed_rate_limiter;

import com.ratelimiter.distributed_rate_limiter.config.RateLimiterProperties;
import com.ratelimiter.distributed_rate_limiter.service.TokenBucketRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBucketRateLimiterTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private DefaultRedisScript<Long> tokenBucketScript;

    private RateLimiterProperties properties;
    private TokenBucketRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        properties = new RateLimiterProperties();
        rateLimiter = new TokenBucketRateLimiter(
                redisTemplate, tokenBucketScript, properties
        );
    }

    @Test
    void shouldAllowRequest_whenTokensAvailable() {
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                anyList(),
                any(String.class),
                any(String.class),
                any(String.class)
        )).thenReturn(1L);

        boolean result = rateLimiter.isAllowed("test-client");

        assertTrue(result, "Request should be allowed when tokens available");
        verify(redisTemplate, times(1)).execute(
                any(DefaultRedisScript.class),
                anyList(),
                any(String.class),
                any(String.class),
                any(String.class)
        );
    }

    @Test
    void shouldBlockRequest_whenNoTokensAvailable() {
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                anyList(),
                any(String.class),
                any(String.class),
                any(String.class)
        )).thenReturn(0L);

        boolean result = rateLimiter.isAllowed("test-client");

        assertFalse(result, "Request should be blocked when no tokens available");
    }

    @Test
    void shouldAllowRequest_whenRedisThrowsException() {
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                anyList(),
                any(String.class),
                any(String.class),
                any(String.class)
        )).thenThrow(new RuntimeException("Redis connection failed"));

        boolean result = rateLimiter.isAllowed("test-client");

        assertTrue(result, "Should allow request when Redis is down (fail open)");
    }

    @Test
    void shouldUseCorrectRedisKey() {
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                eq(Collections.singletonList("rate_limit:my-client")),
                any(String.class),
                any(String.class),
                any(String.class)
        )).thenReturn(1L);

        boolean result = rateLimiter.isAllowed("my-client");

        assertTrue(result);
    }
}