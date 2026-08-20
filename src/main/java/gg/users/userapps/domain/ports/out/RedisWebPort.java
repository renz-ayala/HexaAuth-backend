package gg.users.userapps.domain.ports.out;

public interface RedisWebPort {
    void saveToken(String token, String username);
    String validateToken(String token);
}
