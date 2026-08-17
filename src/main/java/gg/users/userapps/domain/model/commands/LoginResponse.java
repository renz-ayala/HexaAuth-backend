package gg.users.userapps.domain.model.commands;

import java.util.ArrayList;
import java.util.List;

public record LoginResponse (
        String username,
        List<String> roles,
        String message,
        boolean success
){
    public static LoginResponse error() {
        return new LoginResponse(
                null,
                new ArrayList<>(),
                "inválid credentials",
                false
        );
    }

    public static LoginResponse ok(String username, List<String> roles) {
        return new LoginResponse(
                username,
                roles,
                "válid session",
                true
        );
    }
}
