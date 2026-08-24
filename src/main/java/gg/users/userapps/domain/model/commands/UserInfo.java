package gg.users.userapps.domain.model.commands;

import java.util.ArrayList;
import java.util.List;

public record UserInfo(
        String username,
        List<String> roles,
        String message,
        boolean success,
        String firstname,
        String lastname,
        String email
){
    public static UserInfo error() {
        return new UserInfo(
                null,
                new ArrayList<>(),
                "inválid credentials or account isn't confirmed",
                false,
                null,
                null,
                null
        );
    }

    public static UserInfo ok(String username, List<String> roles, String name, String surname, String mail) {
        return new UserInfo(
                username,
                roles,
                "válid session",
                true,
                name,
                surname,
                mail
        );
    }
}
