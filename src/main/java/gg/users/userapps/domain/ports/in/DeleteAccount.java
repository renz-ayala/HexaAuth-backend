package gg.users.userapps.domain.ports.in;

import org.springframework.transaction.annotation.Transactional;

public interface DeleteAccount {
    void execute(String username, String credentials);
}
