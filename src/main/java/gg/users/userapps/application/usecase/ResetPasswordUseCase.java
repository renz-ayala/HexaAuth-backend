package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.ports.in.ResetPassword;
import gg.users.userapps.domain.ports.out.RedisWebPort;
import gg.users.userapps.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordUseCase implements ResetPassword {
    private final UserRepository userRepositoryPort;
    private final RedisWebPort redisWebPort;

    @Override
    @Transactional
    public String execute(String token, String password) {
        try {
            var username = redisWebPort.validateToken("forgot", token);
            boolean isUpdated = userRepositoryPort.resetPassword(username, password);

            if (!isUpdated) {
                throw new IllegalArgumentException("No se ha logrado cambiar la contraseña");
            }

            return username;

        } catch (Exception e) {
            log.error("Error en la operación", e);
            throw new IllegalArgumentException("Link inválido o vencido");
        }
    }
}
