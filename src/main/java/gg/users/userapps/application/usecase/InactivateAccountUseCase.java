package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.ports.in.InactivateAccount;
import gg.users.userapps.domain.ports.out.RedisWebPort;
import gg.users.userapps.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InactivateAccountUseCase implements InactivateAccount {
    private final UserRepository userRepositoryPort;
    private final RedisWebPort redisWebPort;

    @Override
    @Transactional
    public boolean execute(String username, String tokenAccess) {
        redisWebPort.banToken(tokenAccess);
        return userRepositoryPort.activateAccount(username, false);
    }
}
