package gg.users.userapps.infrastructure.adapters.out.web;

import gg.users.userapps.domain.ports.out.RedisWebPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisWebAdapter implements RedisWebPort {
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.expiration.access}")
    private long banedTokenTTL;

    @Override
    @Async
    public void saveToken(String prefixKey, String token, String username) {
        try {
            var redisKey = "%s:%s".formatted(prefixKey, token);
            redisTemplate.opsForValue().set(redisKey, username, Duration.ofMinutes(30));
            log.info("token saved successfully");
        } catch (Exception e) {
            log.error("Error saving token at redis", e);
        }
    }

    @Override
    public String validateToken(String prefixKey, String token) {
        var rediskey = "%s:%s".formatted(prefixKey, token);
        String username = redisTemplate.opsForValue().get(rediskey);

        if (username == null) {
            throw new IllegalArgumentException("El enlace es enválido o ha expirado");
        }

        redisTemplate.delete(rediskey);
        return username;
    }

    @Async
    @Override
    public void banToken(String token) {
        var key = "Lock:%s".formatted(token);
        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(banedTokenTTL));
    }

    @Override
    public boolean isTokenBanned(String token) {
        var key = "Lock:%s".formatted(token);
        return redisTemplate.hasKey(key);
    }

    @Override
    public boolean isAllowedToContinue(String sufixKey, Duration duration) {
        var redisKey = "rate_limit:%s".formatted(sufixKey);
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", duration);
        return Boolean.TRUE.equals(wasSet);
    }
}
