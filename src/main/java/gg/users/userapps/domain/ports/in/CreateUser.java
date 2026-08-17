package gg.users.userapps.domain.ports.in;

import gg.users.userapps.domain.model.commands.CreateUserRequest;
import gg.users.userapps.domain.model.commands.CreateUserResponse;

public interface CreateUser {
    CreateUserResponse execute(CreateUserRequest request);
}