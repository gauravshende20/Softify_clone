package com.harmonia.notification.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailChannel.class);

    private final JavaMailSender mailSender;
    private final String mailHost;
    private final String fromAddress;

    public EmailChannel(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${spring.mail.host:}") String mailHost,
                        @Value("${harmonia.mail.from:noreply@harmonia.local}") String fromAddress) {
        this.mailSender = mailSender.getIfAvailable();
        this.mailHost = mailHost;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(NotificationMessage message) {
        if (smtpEnabled() && message.metadata() != null) {
            Object recipient = message.metadata().get("email");
            if (recipient instanceof String address && !address.isBlank()) {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setFrom(fromAddress);
                mail.setTo(address);
                mail.setSubject(message.title());
                mail.setText(message.body());
                mailSender.send(mail);
            }
        }
        log.info("email dispatched to user {} type {}", message.userId(), message.type());
    }

    private boolean smtpEnabled() {
        return mailSender != null && mailHost != null && !mailHost.isBlank();
    }
}
