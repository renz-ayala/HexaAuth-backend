package gg.users.userapps.domain.ports.out;

import gg.users.userapps.domain.model.User;
import io.jsonwebtoken.Claims;

import java.util.List;

public interface JwtServicePort {
    String generateAccessToken(User user, List<String> roles);

    String generateRefreshToken(User user);

    Claims validateToken(String token);
}
