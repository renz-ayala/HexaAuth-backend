package gg.users.userapps.infrastructure.adapters.out.web;

import gg.users.userapps.domain.ports.out.EmailWebPort;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailWebAdapter implements EmailWebPort {
    private final JavaMailSender javaMailSender;
    private final RestClient restClient;

    @Value("${java.mail.from}")
    private String from;

    @Value("${spring.mail.password}")
    private String apiKey;


    @Async
    public void sendEmailConfirmationWithDomain(String to, String text, String subject) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            javaMailSender.send(message);
            log.info("email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Error sending email to {}", to, e);
        }
    }

    @Override
    @Async
    public void sendEmailConfirmation(String to, String text, String subject) {
        try {
            var body = Map.of(
                    "sender", Map.of("name", "HexaAuth", "email", from),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "htmlContent", text
            );

            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Error sending email to {}", to, e);
        }
    }
}
