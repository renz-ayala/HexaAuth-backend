package gg.users.userapps.domain.model.commands;

import java.util.List;

public record LoginResult(
        LoginResponse response,
        String accessToken,
        String refreshToken
) {
    public static LoginResult error(){
        var errorResponse = LoginResponse.error();
        return new LoginResult(
                errorResponse,
                null,
                null
        );
    }

    public static LoginResult ok(String username, List<String> roles, String accessToken, String refreshToken) {
        var okResponse = LoginResponse.ok(username, roles);
        return new LoginResult(
                okResponse,
                accessToken,
                refreshToken
        );
    }
}
