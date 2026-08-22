package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.model.commands.UserInfo;
import gg.users.userapps.domain.model.commands.LoginResult;
import gg.users.userapps.domain.ports.in.RefreshSession;
import gg.users.userapps.domain.ports.out.JwtServicePort;
import gg.users.userapps.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshSessionUseCase implements RefreshSession {
    private final UserRepository userRepository;
    private final JwtServicePort jwtServicePort;

    @Override
    public LoginResult execute(String username){
       var user = userRepository.getUserByUsername(username);

       if (user == null) {
           return null;
       }

       var roles = userRepository.getRoles(user.getAccountId());

       var loginResponse = UserInfo.ok(
               user.getUsername(),
               roles,
               user.getName(),
               user.getLastName(),
               user.getEmail()
       );
       var newAccessToken = jwtServicePort.generateAccessToken(user, roles);
       var newRefreshToken = jwtServicePort.generateRefreshToken(user);

       return LoginResult.ok(loginResponse, newAccessToken, newRefreshToken);
    }
}
