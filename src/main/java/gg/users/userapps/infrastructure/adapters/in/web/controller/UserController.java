package gg.users.userapps.infrastructure.adapters.in.web.controller;

import gg.users.userapps.domain.model.commands.*;
import gg.users.userapps.domain.ports.in.*;
import gg.users.userapps.infrastructure.adapters.in.web.controller.contracts.ChangePasswordRequest;
import gg.users.userapps.infrastructure.adapters.in.web.controller.contracts.ForgotPasswordRequest;
import gg.users.userapps.infrastructure.adapters.in.web.controller.contracts.ResetPasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final CreateUser createUser;
    private final LoginUser loginUser;
    private final ChangePassword changePassword;
    private final ValidateSession validateSession;
    private final RefreshSession refreshSession;
    private final ConfirmAccount confirmAccount;
    private final FetchUser fetchUser;
    private final RecoverAccount recoverAccount;
    private final ResetPassword resetPassword;

    @Value("${jwt.expiration.access}")
    private long accessExpTime;

    @Value("${jwt.expiration.refresh}")
    private long refreshExpTime;

    @Value("${cookie.security.enabled}")
    private boolean cookieSecurityEnabled;

    @Value("${cookie.security.same-site}")
    private String cookieSameSite;

    @PostMapping(value = "/public/create-user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateUserResponse> createUser(@RequestBody @Valid CreateUserRequest data) {
        var isUserCreated = createUser.execute(data);
        return ResponseEntity.ok(isUserCreated);
    }

    @PostMapping(value = "/public/log-in",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfo> login(@RequestBody @Valid LoginRequest data) {
        var result = loginUser.execute(data.username(), data.password());

        if (!result.response().success()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result.response());
        }

        return this.generateCookies(result);
    }

    @PatchMapping(value = "/change-password", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest data, Authentication authentication) {
        changePassword.execute(data.oldPassword(), data.newPassword1(), authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/public/confirm-account/{token}")
    public ResponseEntity<String> confirmAccount(@PathVariable String token) {
        boolean wasUpdated = confirmAccount.execute(token);

        if (!wasUpdated) {
            return ResponseEntity.badRequest().body("Hubo un error verificando la cuenta");
        }

        return ResponseEntity.ok("Cuenta verificada");
    }

    @PostMapping(value = "/public/forgot-password")
    public ResponseEntity<Void> recoverAccount(@Valid @RequestBody ForgotPasswordRequest request) {
        recoverAccount.execute(request.username());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/public/reset-password")
    public ResponseEntity<UserInfo> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        var username = resetPassword.execute(request.token(), request.password());
        var result = loginUser.execute(username, request.password());

        if (!result.response().success()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result.response());
        }

        return this.generateCookies(result);
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfo> checkSession(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var username = authentication.getName();
        var user = fetchUser.execute(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping(value = "/public/refresh")
    public ResponseEntity<UserInfo> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var claims = validateSession.execute(refreshToken);

        if (claims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var username = claims.getSubject();
        var result = refreshSession.execute(username);

        return this.generateCookies(result);
    }

    @PostMapping(value = "/logout")
    public ResponseEntity<Void> logout() {
        var clearAccessCookie = this.putCookie("access_token", "", 0);
        var clearRefreshCookie = this.putCookie("refresh_token", "", 0);

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, clearAccessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString());

        return ResponseEntity.noContent().headers(headers).build();
    }

    @NonNull
    private ResponseEntity<UserInfo> generateCookies(LoginResult result) {
        var accessCookie = this.putCookie("access_token", result.accessToken(), accessExpTime);
        var refreshCookie = this.putCookie("refresh_token", result.refreshToken(), refreshExpTime);

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.response());
    }

    private ResponseCookie putCookie(String name,String token, long time) {
        var builder = ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(cookieSecurityEnabled)
                .path("/")
                .maxAge(time * 60);

        if (cookieSameSite != null && !cookieSameSite.isBlank()) {
            builder.sameSite(cookieSameSite);
        }

        return builder.build();
    }
}
