package gg.users.userapps.application.usecase;

import gg.users.userapps.domain.ports.in.ChangePassword;
import gg.users.userapps.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangePasswordUseCase implements ChangePassword {

    private final UserRepository UserRepository;

    @Override
    public void execute(String oldPassword, String newPassword, String username){
        UserRepository.changePassword(username, oldPassword, newPassword);
    }
}
