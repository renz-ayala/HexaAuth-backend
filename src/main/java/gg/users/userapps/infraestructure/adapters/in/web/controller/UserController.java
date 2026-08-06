package gg.users.userapps.infraestructure.adapters.in.web.controller;

import gg.users.userapps.domain.ports.in.ChangePassword;
import gg.users.userapps.domain.ports.in.CreateUser;
import gg.users.userapps.domain.ports.in.LoginUser;
import gg.users.userapps.infraestructure.adapters.in.web.request.ChangePasswordRequest;
import gg.users.userapps.infraestructure.adapters.in.web.request.CreateUserRequest;
import gg.users.userapps.infraestructure.adapters.in.web.request.LoginRequest;
import gg.users.userapps.infraestructure.adapters.in.web.response.CreateUserResponse;
import gg.users.userapps.infraestructure.adapters.in.web.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final CreateUser createUser;
    private final LoginUser loginUser;
    private final ChangePassword changePassword;

    @PostMapping(value = "/public/create-user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateUserResponse createUser(@RequestBody CreateUserRequest data) {
        return createUser.execute(data);
    }

    @PostMapping(value = "/public/log-in",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(@RequestBody LoginRequest data) {
        return loginUser.execute(data.username(), data.password());
    }

    @PostMapping(value = "/change-password", consumes = "application/json", produces = "application/json")
    public void changePassword(@RequestBody ChangePasswordRequest data, Authentication authentication) {
        changePassword.execute(data.oldPassword(), data.newPassword(), authentication.getName());
    }
}
