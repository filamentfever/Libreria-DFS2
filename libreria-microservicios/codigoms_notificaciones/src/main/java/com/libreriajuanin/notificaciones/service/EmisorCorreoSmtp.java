package com.libreriajuanin.notificaciones.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Envío real por SMTP: activar con app.correo.modo=smtp y configurar spring.mail.*. */
@Component
@ConditionalOnProperty(name = "app.correo.modo", havingValue = "smtp")
public class EmisorCorreoSmtp implements EmisorCorreo {

    private final JavaMailSender mailSender;
    private final String remitente;

    public EmisorCorreoSmtp(JavaMailSender mailSender, @Value("${app.correo.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviar(String destinatario, String asunto, String mensaje) {
        SimpleMailMessage correo = new SimpleMailMessage();
        correo.setFrom(remitente);
        correo.setTo(destinatario);
        correo.setSubject(asunto);
        correo.setText(mensaje);
        mailSender.send(correo);
    }
}
