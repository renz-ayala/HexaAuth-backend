package gg.users.userapps.infrastructure.adapters.in.web.controller;

import gg.users.userapps.domain.model.commands.*;
import gg.users.userapps.domain.ports.in.*;
import gg.users.userapps.infrastructure.adapters.in.web.controller.contracts.ChangePasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final CreateUser createUser;
    private final LoginUser loginUser;
    private final ChangePassword changePassword;
    private final ValidateSession validateSession;
    private final RefreshSession refreshSession;

    @Value("${jwt.expiration.access}")
    private long accessExpTime;

    @Value("${jwt.expiration.refresh}")
    private long refreshExpTime;

    @Value("${cookie.security.enabled}")
    private boolean cookieSecurityEnabled;

    @Value("${cookie.security.same-site}")
    private String cookieSameSite;

    @PostMapping(value = "/public/create-user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateUserResponse createUser(@RequestBody @Valid CreateUserRequest data) {
        return createUser.execute(data);
    }

    @PostMapping(value = "/public/log-in",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest data) {
        var result = loginUser.execute(data.username(), data.password());

        if (!result.response().success()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result.response());
        }

        return this.generateCookies(result);
    }

    @PatchMapping(value = "/change-password", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest data, Authentication authentication) {
        changePassword.execute(data.oldPassword(), data.newPassword(), authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> checkSession(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var username = authentication.getName();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var loginResponse = LoginResponse.ok(username, roles);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping(value = "/public/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
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
    private ResponseEntity<LoginResponse> generateCookies(LoginResult result) {
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
