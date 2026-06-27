local key = KEYS[1]
local now = tonumber(ARGV[1])
local window_size = tonumber(ARGV[2])
local max_requests = tonumber(ARGV[3])
local window_start = now - (window_size * 1000)

-- Remove old requests outside the window
redis.call("ZREMRANGEBYSCORE", key, 0, window_start)

-- Count requests in current window
local count = redis.call("ZCARD", key)

if count < max_requests then
    -- Add current request
    redis.call("ZADD", key, now, now)
    redis.call("EXPIRE", key, window_size)
    return 1
else
    return 0
end