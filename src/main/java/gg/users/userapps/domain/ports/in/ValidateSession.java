package gg.users.userapps.domain.ports.in;

import io.jsonwebtoken.Claims;

public interface ValidateSession {
    Claims execute(String token);
}
