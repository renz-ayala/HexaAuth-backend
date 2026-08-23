package gg.users.userapps.domain.ports.in;

public interface ResetPassword {
    String execute(String token, String password);
}
