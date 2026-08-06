package gg.users.userapps.domain.ports.in;

import gg.users.userapps.infraestructure.adapters.in.web.request.CreateUserRequest;
import gg.users.userapps.infraestructure.adapters.in.web.response.CreateUserResponse;

public interface CreateUser {
    CreateUserResponse execute(CreateUserRequest request);
}