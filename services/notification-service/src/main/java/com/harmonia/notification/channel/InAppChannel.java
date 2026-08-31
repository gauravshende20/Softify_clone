package com.harmonia.notification.channel;

import com.harmonia.notification.domain.Notification;
import com.harmonia.notification.repo.NotificationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InAppChannel implements NotificationChannel {

    private final NotificationRepository notifications;

    public InAppChannel(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Override
    @Transactional
    public void send(NotificationMessage message) {
        notifications.save(new Notification(
                message.userId(),
                message.type(),
                message.title(),
                message.body(),
                message.metadata()));
    }
}
