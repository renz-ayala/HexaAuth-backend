package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.ports.in.RecoverAccount;
import gg.users.userapps.domain.ports.out.EmailWebPort;
import gg.users.userapps.domain.ports.out.RedisWebPort;
import gg.users.userapps.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoverAccountUseCase implements RecoverAccount {
    private final UserRepository userRepositoryPort;
    private final RedisWebPort redisWebPort;
    private final EmailWebPort emailWebPort;

    @Value("${cors.origin.allowed}")
    private String frontendUrl;

    @Override
    public void execute(String username) {
        var user = userRepositoryPort.getUserByUsername(username);

        if (user == null) {
            log.warn("There isn't an account with {}", username);
            throw new IllegalArgumentException("Sucedió un error recuperando la cuenta");
        }

        var email = user.getEmail();
        var token = UUID.randomUUID().toString();
        String text =  this.getResetPasswordText(user.getName(), token);

        emailWebPort.sendEmailConfirmation(email, text, "Restablecer contraseña");
        redisWebPort.saveToken("forgot", token, username);
    }

    @NonNull
    private String getResetPasswordText(String name, String token) {
        var resetUrl = "%s/reset-password?token=%s".formatted(frontendUrl, token);

        return """
            <div style='font-family: sans-serif; background-color: #09090b; color: #f4f4f5; padding: 24px; border-radius: 8px;'>
                <h2>Hola, %s</h2>
                <p>Hemos recibido una solicitud para restablecer tu contraseña. Haz clic en el siguiente enlace para actualizarla:</p>
                <a href='%s' style='display: inline-block; background-color: #f4f4f5; color: #09090b; padding: 10px 18px; border-radius: 6px; text-decoration: none; font-weight: bold;'>Restablecer contraseña</a>
                <p style='font-size: 12px; color: #a1a1aa; margin-top: 16px;'>Este enlace expirará en 15 minutos. Si no solicitaste este cambio, ignora este correo.</p>
            </div>
            """.formatted(name, resetUrl);
    }
}
