package gg.users.userapps.domain.ports.out;

public interface EmailWebPort {
    void sendEmailConfirmation(String to, String text, String subject);
}
