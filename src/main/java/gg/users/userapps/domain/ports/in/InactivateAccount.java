package gg.users.userapps.domain.ports.in;

public interface InactivateAccount {
    boolean execute(String username, String tokenAccess);
}
