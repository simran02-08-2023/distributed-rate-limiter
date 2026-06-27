package com.ratelimiter.distributed_rate_limiter.controller;

import com.ratelimiter.distributed_rate_limiter.service.SlidingWindowRateLimiter;
import com.ratelimiter.distributed_rate_limiter.service.TokenBucketRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemoController {

    private final TokenBucketRateLimiter tokenBucketRateLimiter;
    private final SlidingWindowRateLimiter slidingWindowRateLimiter;

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello(
            @RequestHeader(value = "X-API-Key", defaultValue = "anonymous") String apiKey) {

        if (!tokenBucketRateLimiter.isAllowed(apiKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Token bucket limit exceeded"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Request allowed via token bucket!",
                "client", apiKey,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/sliding")
    public ResponseEntity<Map<String, String>> sliding(
            @RequestHeader(value = "X-API-Key", defaultValue = "anonymous") String apiKey) {

        if (!slidingWindowRateLimiter.isAllowed(apiKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Sliding window limit exceeded",
                            "limit", "10 requests per 60 seconds"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Request allowed via sliding window!",
                "client", apiKey,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}