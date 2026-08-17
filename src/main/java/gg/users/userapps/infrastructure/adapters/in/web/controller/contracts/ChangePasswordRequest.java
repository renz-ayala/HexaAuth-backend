package gg.users.userapps.infrastructure.adapters.in.web.controller.contracts;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {}
