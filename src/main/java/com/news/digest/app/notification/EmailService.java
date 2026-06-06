package com.news.digest.app.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {


    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:localuser985@mail.com}")
    private String fromEmail;

    @Value("${app.name:News Digest}")
    private String appName;

    /**
     * Send an HTML email asynchronously using a Thymeleaf template.
     * @param to         recipient email
     * @param subject    email subject
     * @param template   template name under resources/templates/email/
     * @param variables  variables injected into the template
     */
    @Async
    public void sendHtml(String to, String subject, String template, Map<String, Object> variables) {
        try {
            Context ctx = new Context();
            ctx.setVariables(variables);
            ctx.setVariable("appName", appName);

            String htmlBody = templateEngine.process("email/" + template, ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("[Email] Sent '{}' to {}", subject, to);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("[Email] Failed to send '{}' to {}: {}", subject, to, e.getMessage());
        }
    }

    /**
     * Send a plain-text email (fallback, no template needed).
     */
    @Async
    public void sendPlain(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
            log.info("[Email] Sent plain '{}' to {}", subject, to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("[Email] Failed to send plain email to {}: {}", to, e.getMessage());
        }
    }

}
