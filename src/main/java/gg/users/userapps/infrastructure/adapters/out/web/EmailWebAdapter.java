package gg.users.userapps.infrastructure.adapters.out.web;

import gg.users.userapps.domain.ports.out.EmailWebPort;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailWebAdapter implements EmailWebPort {
    private final JavaMailSender javaMailSender;
    
    @Value("${cors.origin.allowed}")
    private String frontendUrl;

    @Value("${java.mail.from}")
    private String from;

    @Override
    @Async
    public void sendEmailConfirmation(String to, String name, String token) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            String text = this.getText(name, token);

            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Confirm your account");
            helper.setText(text, true);

            javaMailSender.send(message);
            log.info("email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Error sending email to {}", to, e);
        }
    }

    @NonNull
    private String getText(String name, String token) {
        var confirmationUrl = "%s/confirm-account?token=%s".formatted(frontendUrl, token);

        return  """
                <div style='font-family: sans-serif; background-color: #09090b; color: #f4f4f5; padding: 24px; border-radius: 8px;'>
                    <h2>Hi, %s </h2>
                    <p>Click the next link to confirm your account:</p>
                    <a href='%s' style='display: inline-block; background-color: #f4f4f5; color: #09090b; padding: 10px 18px; border-radius: 6px; text-decoration: none; font-weight: bold;'>Confirm account</a>
                </div>
                """.formatted(name, confirmationUrl);
    }
}
