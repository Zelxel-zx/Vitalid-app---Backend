package com.vitalid.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {
    private final JavaMailSender mailSender;

    @Value("${app.notifications.enabled:false}")
    private boolean enabled;

    @Value("${app.notifications.from:no-reply@vitalid.test}")
    private String from;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notifications disabled");
        }
        if (to == null || to.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient required");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject == null ? "Vitalid Notification" : subject);
        message.setText(body == null ? "" : body);

        mailSender.send(message);
    }
}


