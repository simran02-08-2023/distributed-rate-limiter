# Distributed Rate Limiter

A production-grade distributed rate limiter built with Java, Spring Boot, and Redis.
Implements both Token Bucket and Sliding Window algorithms with atomic Lua scripts.

## Tech Stack
- Java 17
- Spring Boot 3.3.5
- Redis 7 (via Docker)
- Lua scripting (atomic operations)
- JUnit 5 + Mockito

## Algorithms Implemented

### Token Bucket
- Allows burst traffic up to a configured capacity
- Refills tokens at a fixed rate per second
- Stored in Redis HASH with atomic Lua script

### Sliding Window
- Tracks exact request count in a rolling time window
- No burst allowed — strict request counting
- Stored in Redis ZSET (sorted set by timestamp)

## Architecture
                    Client

                       │

             HTTP Request

                       │

            Spring Boot API

                       │

             RateLimitFilter

                       │

        ┌──────────────┴──────────────┐
        │                             │
 Token Bucket                  Sliding Window
        │                             │
        └──────────────┬──────────────┘
                       │
              Atomic Lua Script
                       │
                    Redis
                       │
            Allow (200) / Block (429)
## API Endpoints

| Method | Endpoint      | Description                        |
|--------|---------------|------------------------------------|
| GET    | /api/hello    | Token bucket rate limited endpoint |
| GET    | /api/sliding  | Sliding window rate limited endpoint |
| GET    | /actuator/health | Health check with Redis status  |

## Configuration

```yaml
rate-limiter:
  default:
    capacity: 100      # max tokens (burst limit)
    refill-rate: 10    # tokens added per second
  tiers:
    premium:
      capacity: 1000
      refill-rate: 100
    free:
      capacity: 20
      refill-rate: 2
```

## Running Locally

### Prerequisites
- Java 17
- Docker

### Steps

```bash
# Start Redis
docker run -d --name redis-rl -p 6379:6379 redis:7-alpine

# Run the application
./mvnw spring-boot:run

# Test token bucket
curl http://localhost:8080/api/hello

# Test sliding window
curl http://localhost:8080/api/sliding

# Health check
curl http://localhost:8080/actuator/health
```

## Running Tests

```bash
./mvnw test
```

## Key Design Decisions

| Decision | Reason |
|----------|--------|
| Lua scripts for Redis operations | Atomic execution prevents race conditions across multiple server instances |
| Fail-open on Redis errors | Availability over strict rate limiting — requests allowed if Redis is down |
| Token bucket for /api/hello | Allows burst traffic for better user experience |
| Sliding window for /api/sliding | Strict enforcement for sensitive endpoints |
| Client ID from X-API-Key header | Falls back to IP address if no API key provided |

## What I Learned
- How distributed systems handle shared state across multiple instances
- Why atomicity matters in concurrent environments
- Tradeoffs between Token Bucket vs Sliding Window algorithms
- Spring Boot filter chain and request interception
- Redis data structures (HASH for token bucket, ZSET for sliding window)
