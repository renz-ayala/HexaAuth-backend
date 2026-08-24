package gg.users.userapps.infrastructure.adapters.in.web.contracts;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank String newPassword1,
        @NotBlank String newPassword2
) {
    public ChangePasswordRequest {
        if (newPassword1 != null && !newPassword1.equals(newPassword2)) {
            throw new IllegalArgumentException("No hay coincidencia de contraseña");
        }

        if (newPassword1 != null && newPassword1.equals(oldPassword)) {
            throw new IllegalArgumentException("La contraseña antigua y nueva son iguales");
        }

        if (newPassword1 == null || newPassword1.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
    }
}
