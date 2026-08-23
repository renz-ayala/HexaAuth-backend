package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.model.User;
import gg.users.userapps.domain.ports.in.CreateUser;
import gg.users.userapps.domain.ports.out.EmailWebPort;
import gg.users.userapps.domain.ports.out.RedisWebPort;
import gg.users.userapps.domain.ports.out.UserRepository;
import gg.users.userapps.domain.model.commands.CreateUserRequest;
import gg.users.userapps.domain.model.commands.CreateUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateUserUseCase implements CreateUser {
    private final UserRepository userRepository;
    private final EmailWebPort emailWebPort;
    private final RedisWebPort redisWebPort;

    @Value("${cors.origin.allowed}")
    private String frontendUrl;

    @Override
    @Transactional
    public CreateUserResponse execute(CreateUserRequest request) {
        var user = new User();
        user.validateUsername(request.username());
        user.validatePassword(request.password());

        user.setUsername(request.username());
        user.setPassword(request.password());
        user.setName(request.name());
        user.setLastName(request.lastName());
        user.setEmail(request.email());

        var response = userRepository.createUser(user);

        if (response.success()) {
            var token = UUID.randomUUID().toString();
            String text = this.getText(user.getName(), token);

            redisWebPort.saveToken("confirm", token, user.getUsername());
            emailWebPort.sendEmailConfirmation(user.getEmail(), text, "Confirm your account");
        }

        return response;
    }

    @NonNull
    private String getText(String name, String token) {
        var confirmationUrl = "%s/confirm-account?token=%s".formatted(frontendUrl, token);

        return  """
                <div style='font-family: sans-serif; background-color: #09090b; color: #f4f4f5; padding: 24px; border-radius: 8px;'>
                    <h2>Hi, %s </h2>
                    <p>Click the next link to confirm your account:</p>
                    <a href='%s' style='display: inline-block; background-color: #f4f4f5; color: #09090b; padding: 10px 18px; border-radius: 6px; text-decoration: none; font-weight: bold;'>Confirm account</a>
                </div>
                """.formatted(name, confirmationUrl);
    }
}