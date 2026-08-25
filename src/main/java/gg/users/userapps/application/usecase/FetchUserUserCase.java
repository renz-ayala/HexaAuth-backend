package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.model.commands.UserInfo;
import gg.users.userapps.domain.ports.in.FetchUser;
import gg.users.userapps.domain.ports.out.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FetchUserUserCase implements FetchUser {
    private final UserRepository userRepositoryPort;

    @Override
    public UserInfo execute(String username) {
        var user = userRepositoryPort.getUserByUsername(username);

        if (user == null) {
            throw new EntityNotFoundException("Usuario %s no encontrado".formatted(username));
        }

        var roles = userRepositoryPort.getRoles(user.getAccountId());
        return UserInfo.ok(
                user.getUsername(),
                roles,
                user.getName(),
                user.getLastName(),
                user.getEmail()
        );
    }
}
