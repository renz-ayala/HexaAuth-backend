package gg.users.userapps.infraestructure.adapters.in.web.request;

public record CreateUserRequest(
        String username,
        String password,
        String name,
        String lastName,
        String email
) {}