package gg.users.userapps.domain.ports.out;

public interface RedisWebPort {
    void saveToken(String prefixKey, String token, String username);
    String validateToken(String prefixKey, String token);
}
