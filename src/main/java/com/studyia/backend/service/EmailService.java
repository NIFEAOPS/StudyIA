package com.studyia.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        @Value("${studyia.mail.enabled:false}") boolean enabled,
                        @Value("${studyia.mail.from:no-reply@studyia.local}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public void sendVerificationCode(String recipient, String code) {
        if (!enabled) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("Confirma tu correo en StudyIA");
        message.setText("Tu código de verificación es: " + code + "\n\nCaduca en 10 minutos.");
        mailSender.send(message);
    }

    public void sendPasswordResetLink(String recipient, String resetLink) {
        if (!enabled) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("Restablece tu contraseña de StudyIA");
        message.setText("Solicitaste cambiar tu contraseña de StudyIA.\n\n"
                + "Abre este enlace para crear una nueva contraseña:\n" + resetLink
                + "\n\nEl enlace caduca en 10 minutos y solo puede utilizarse una vez."
                + "\n\nSi no solicitaste este cambio, ignora este correo.");
        mailSender.send(message);
    }
}