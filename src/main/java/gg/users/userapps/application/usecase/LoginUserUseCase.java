package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.model.commands.LoginResult;
import gg.users.userapps.domain.ports.in.LoginUser;
import gg.users.userapps.domain.ports.out.JwtServicePort;
import gg.users.userapps.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginUserUseCase implements LoginUser {
    private final UserRepository userRepository;
    private final JwtServicePort jwtService;

    @Override
    public LoginResult execute(String username, String password) {
        var user = userRepository.findUser(username, password);

        if (user == null) {
            return LoginResult.error();
        }

        var roles = userRepository.getRoles(user.getAccountId());

        var accessToken = jwtService.generateAccessToken(user, roles);
        var refreshToken = jwtService.generateRefreshToken(user);

        return LoginResult.ok(user.getUsername(), roles, accessToken, refreshToken);
    }
}
