package com.ratelimiter.distributed_rate_limiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private TierConfig defaultConfig = new TierConfig();
    private Map<String, TierConfig> tiers = new HashMap<>();

    @Data
    public static class TierConfig {
        private long capacity = 100;
        private long refillRate = 10;
    }

    public TierConfig getConfigForClient(String tier) {
        return tiers.getOrDefault(tier, defaultConfig);
    }
}