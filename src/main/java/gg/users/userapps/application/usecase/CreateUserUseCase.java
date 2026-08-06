package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.model.User;
import gg.users.userapps.domain.ports.in.CreateUser;
import gg.users.userapps.domain.ports.out.UserRepository;
import gg.users.userapps.infraestructure.adapters.in.web.request.CreateUserRequest;
import gg.users.userapps.infraestructure.adapters.in.web.response.CreateUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateUserUseCase implements CreateUser {

    private final UserRepository userRepository;

    @Override
    public CreateUserResponse execute(CreateUserRequest request) {
        CreateUserResponse response = new CreateUserResponse();

        User user = new User();
        user.validateUsername(request.username());
        user.validatePassword(request.password());

        if (userRepository.userExists(request.username())) {
            response.setMessage("El usuario ya existe");
            response.setCode(1);
            return response;
        }

        user.setUsername(request.username());
        user.setPassword(request.password());
        user.setName(request.name());
        user.setLastName(request.lastName());
        user.setEmail(request.email());

        userRepository.createUser(user);

        response.setMessage("Usuario creado exitosamente");
        response.setCode(2);

        return response;
    }
}