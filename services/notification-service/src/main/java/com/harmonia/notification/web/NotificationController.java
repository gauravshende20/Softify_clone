package com.harmonia.notification.web;

import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.common.security.CurrentUser;
import com.harmonia.notification.dto.NotificationResponse;
import com.harmonia.notification.dto.PreferenceResponse;
import com.harmonia.notification.dto.UpdatePreferencesRequest;
import com.harmonia.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public PageResponse<NotificationResponse> list(
            CurrentUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return notifications.list(user.id(), page, size);
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(CurrentUser user, @PathVariable UUID id) {
        return notifications.markRead(user.id(), id);
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(CurrentUser user) {
        notifications.markAllRead(user.id());
    }

    @GetMapping("/preferences")
    public PreferenceResponse getPreferences(CurrentUser user) {
        return notifications.getPreferences(user.id());
    }

    @PutMapping("/preferences")
    public PreferenceResponse updatePreferences(CurrentUser user, @Valid @RequestBody UpdatePreferencesRequest request) {
        return notifications.updatePreferences(user.id(), request);
    }
}
