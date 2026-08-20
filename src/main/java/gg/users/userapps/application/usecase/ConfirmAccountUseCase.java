package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.ports.in.ConfirmAccount;
import gg.users.userapps.domain.ports.out.RedisWebPort;
import gg.users.userapps.domain.ports.out.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmAccountUseCase implements ConfirmAccount {
    private final RedisWebPort redisWebPort;
    private final UserRepository userRepositoryPort;

    @Override
    @Transactional
    public boolean execute(String token) {
        try {
            String username = redisWebPort.validateToken(token);
            return userRepositoryPort.confirmAccount(username);
        } catch (Exception e) {
            log.error("Error validando en redis", e);
            return false;
        }
    }
}
