package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.model.User;
import gg.users.userapps.domain.ports.in.CreateUser;
import gg.users.userapps.domain.ports.out.EmailWebPort;
import gg.users.userapps.domain.ports.out.RedisWebPort;
import gg.users.userapps.domain.ports.out.UserRepository;
import gg.users.userapps.domain.model.commands.CreateUserRequest;
import gg.users.userapps.domain.model.commands.CreateUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateUserUseCase implements CreateUser {
    private final UserRepository userRepository;
    private final EmailWebPort emailWebPort;
    private final RedisWebPort redisWebPort;

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
            redisWebPort.saveToken(token, user.getUsername());
            emailWebPort.sendEmailConfirmation(user.getEmail(), user.getName(), token);
        }

        return response;
    }
}