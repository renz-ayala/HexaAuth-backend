package gg.users.userapps.domain.ports.in;

import gg.users.userapps.domain.model.commands.UserInfo;

public interface FetchUser {
    UserInfo execute(String username);
}
