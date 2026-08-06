package gg.users.userapps.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class User {
    private Long accountId;
    private String username;
    private String password;
    private String name;
    private String lastName;
    private String email;
    private LocalDateTime tsCrea;

    public void validateNewPassword(String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres.");
        }
        if (newPassword.equals(oldPassword)) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual.");
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
    }

    public void validateUsername(String username) {
        if (username == null || username.length() < 4) {
            throw new IllegalArgumentException("El nombre de usuario debe tener al menos 5 caracteres.");
        }
    }
}
