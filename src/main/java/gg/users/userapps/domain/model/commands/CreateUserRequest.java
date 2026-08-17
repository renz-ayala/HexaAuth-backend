package gg.users.userapps.domain.model.commands;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String name,
        @NotBlank String lastName,
        @NotBlank String email
) {}