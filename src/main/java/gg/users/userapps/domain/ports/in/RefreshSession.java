package gg.users.userapps.domain.ports.in;

import gg.users.userapps.domain.model.commands.LoginResult;

public interface RefreshSession {
    LoginResult execute(String username);
}
