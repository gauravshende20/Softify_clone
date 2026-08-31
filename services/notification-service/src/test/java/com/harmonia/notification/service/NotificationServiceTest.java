package com.harmonia.notification.service;

import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.EventType;
import com.harmonia.notification.channel.EmailChannel;
import com.harmonia.notification.channel.InAppChannel;
import com.harmonia.notification.channel.NotificationMessage;
import com.harmonia.notification.domain.Notification;
import com.harmonia.notification.kafka.NotificationEventConsumer;
import com.harmonia.notification.repo.NotificationPreferenceRepository;
import com.harmonia.notification.repo.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notifications;
    @Mock NotificationPreferenceRepository preferences;
    @Mock InAppChannel inAppChannel;
    @Mock EmailChannel emailChannel;

    NotificationService service;
    NotificationEventConsumer consumer;

    @BeforeEach
    void setUp() {
        service = new NotificationService(notifications, preferences, inAppChannel, emailChannel);
        consumer = new NotificationEventConsumer(service);
    }

    @Test
    void userRegisteredDispatchesWelcomeInAppAndEmail() {
        UUID userId = UUID.randomUUID();
        when(preferences.findById(userId)).thenReturn(Optional.empty());

        consumer.handle(DomainEvent.of(
                EventType.USER_REGISTERED, "Account", userId.toString(),
                "auth-service", "trace", userId.toString(),
                Map.of("email", "ada@harmonia.local", "role", "LISTENER", "verificationToken", "secret")));

        ArgumentCaptor<NotificationMessage> inApp = ArgumentCaptor.forClass(NotificationMessage.class);
        ArgumentCaptor<NotificationMessage> email = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(inAppChannel).send(inApp.capture());
        verify(emailChannel).send(email.capture());
        NotificationMessage message = inApp.getValue();
        assertEquals("USER_REGISTERED", message.type());
        assertEquals("Welcome to Harmonia", message.title());
        assertFalse(message.metadata().containsKey("verificationToken"));
        assertFalse(message.metadata().containsKey("email"));
        assertEquals("ada@harmonia.local", email.getValue().metadata().get("email"));
    }

    @Test
    void albumPublishedWithoutFollowersDoesNotFanout() {
        consumer.handle(DomainEvent.of(
                EventType.ALBUM_PUBLISHED, "Album", UUID.randomUUID().toString(),
                "catalog-service", "trace", UUID.randomUUID().toString(),
                Map.of("title", "Night Drive")));
        verify(inAppChannel, never()).send(any());
        verify(emailChannel, never()).send(any());
    }

    @Test
    void albumPublishedFansOutToFollowerUserIds() {
        UUID followerOne = UUID.randomUUID();
        UUID followerTwo = UUID.randomUUID();
        when(preferences.findById(any())).thenReturn(Optional.empty());

        consumer.handle(DomainEvent.of(
                EventType.ALBUM_PUBLISHED, "Album", UUID.randomUUID().toString(),
                "catalog-service", "trace", UUID.randomUUID().toString(),
                Map.of("followerUserIds", List.of(followerOne.toString(), followerTwo.toString()))));

        verify(inAppChannel, times(2)).send(any(NotificationMessage.class));
    }

    @Test
    void collaborativePlaylistUpdateNotifiesCollaborators() {
        UUID collaborator = UUID.randomUUID();
        when(preferences.findById(collaborator)).thenReturn(Optional.empty());
        consumer.handle(DomainEvent.of(
                EventType.PLAYLIST_UPDATED, "Playlist", UUID.randomUUID().toString(),
                "playlist-service", "trace", UUID.randomUUID().toString(),
                Map.of("collaborative", true, "collaboratorIds", List.of(collaborator.toString()))));
        verify(inAppChannel).send(any(NotificationMessage.class));
    }

    @Test
    void markReadUpdatesOwnedNotification() {
        UUID userId = UUID.randomUUID();
        Notification stored = new Notification(userId, "USER_REGISTERED", "Welcome", "Hello", Map.of());
        when(notifications.findByIdAndUserId(stored.getId(), userId)).thenReturn(Optional.of(stored));
        service.markRead(userId, stored.getId());
        assert stored.isReadFlag();
    }
}
