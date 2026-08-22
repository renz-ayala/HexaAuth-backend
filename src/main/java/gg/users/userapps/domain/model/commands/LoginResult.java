package gg.users.userapps.domain.model.commands;

public record LoginResult(
        UserInfo response,
        String accessToken,
        String refreshToken
) {
    public static LoginResult error(){
        var errorResponse = UserInfo.error();
        return new LoginResult(
                errorResponse,
                null,
                null
        );
    }

    public static LoginResult ok(UserInfo response, String accessToken, String refreshToken) {
        return new LoginResult(
                response,
                accessToken,
                refreshToken
        );
    }
}
