package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.ports.in.ValidateSession;
import gg.users.userapps.domain.ports.out.JwtServicePort;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateSessionUseCase implements ValidateSession {
    private final JwtServicePort jwtServicePort;

    @Override
    public Claims execute(String token) {
        return jwtServicePort.validateToken(token);
    }
}
