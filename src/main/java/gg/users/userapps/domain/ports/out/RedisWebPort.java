package gg.users.userapps.domain.ports.out;

import org.springframework.scheduling.annotation.Async;

import java.time.Duration;

public interface RedisWebPort {
    void saveToken(String prefixKey, String token, String username);
    String validateToken(String prefixKey, String token);
    void banToken(String token);
    boolean isTokenBanned(String token);
    boolean isAllowedToContinue(String sufixKey, Duration duration);
}
