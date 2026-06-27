package com.ratelimiter.distributed_rate_limiter.service;

public interface RateLimiterService {

    boolean isAllowed(String clientId);
}