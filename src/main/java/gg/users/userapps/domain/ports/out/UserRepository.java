package gg.users.userapps.domain.ports.out;

import gg.users.userapps.domain.model.User;
import gg.users.userapps.domain.model.commands.CreateUserResponse;

import java.util.List;

public interface UserRepository {
    CreateUserResponse createUser(User user);
    User findUser(String username, String password);
    User getUserByUsername(String username);
    boolean activateAccount(String username, boolean toActive);
    void changePassword(String username, String oldPassword, String newPassword);
    List<String> getRoles(Long userId);
    boolean resetPassword(String username, String newPassword);
    void deleteUser(String username);
}
