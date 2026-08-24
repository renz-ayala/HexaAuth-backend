package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.ports.in.DeleteAccount;
import gg.users.userapps.domain.ports.out.RedisWebPort;
import gg.users.userapps.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAccountUseCase implements DeleteAccount {
    private final UserRepository userRepositoryPort;
    private final RedisWebPort redisWebPort;

    @Transactional
    @Override
    public void execute(String username, String credentials) {
        userRepositoryPort.deleteUser(username);
        redisWebPort.banToken(credentials);
    }
}
