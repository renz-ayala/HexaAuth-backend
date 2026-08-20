package gg.users.userapps.domain.ports.in;

public interface ConfirmAccount {
    boolean execute(String token);
}
