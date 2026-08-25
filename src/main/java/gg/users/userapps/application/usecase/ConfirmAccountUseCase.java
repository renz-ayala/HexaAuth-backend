package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.ports.in.ConfirmAccount;
import gg.users.userapps.domain.ports.out.RedisWebPort;
import gg.users.userapps.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmAccountUseCase implements ConfirmAccount {
    private final RedisWebPort redisWebPort;
    private final UserRepository userRepositoryPort;

    @Override
    @Transactional
    public boolean execute(String token) {
        String username = redisWebPort.validateToken("confirm", token);

        if (username == null || username.isBlank()) {
            return false;
        }

        return userRepositoryPort.activateAccount(username, true);
    }
}
