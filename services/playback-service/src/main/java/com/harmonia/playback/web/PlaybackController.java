package com.harmonia.playback.web;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.security.CurrentUser;
import com.harmonia.playback.dto.PlayRequest;
import com.harmonia.playback.dto.PlaybackStateResponse;
import com.harmonia.playback.dto.QueueResponse;
import com.harmonia.playback.dto.QueueTrackRequest;
import com.harmonia.playback.dto.RepeatRequest;
import com.harmonia.playback.dto.SeekRequest;
import com.harmonia.playback.dto.ShuffleRequest;
import com.harmonia.playback.dto.StreamUrlResponse;
import com.harmonia.playback.service.PlaybackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/playback")
public class PlaybackController {

    private final PlaybackService playbackService;

    public PlaybackController(PlaybackService playbackService) {
        this.playbackService = playbackService;
    }

    @PostMapping("/play")
    public PlaybackStateResponse play(CurrentUser user, @Valid @RequestBody PlayRequest request) {
        return playbackService.play(requireUser(user), request);
    }

    @PostMapping("/pause")
    public PlaybackStateResponse pause(CurrentUser user) {
        return playbackService.pause(requireUser(user));
    }

    @PostMapping("/resume")
    public PlaybackStateResponse resume(CurrentUser user) {
        return playbackService.resume(requireUser(user));
    }

    @PostMapping("/seek")
    public PlaybackStateResponse seek(CurrentUser user, @Valid @RequestBody SeekRequest request) {
        return playbackService.seek(requireUser(user), request.positionMs());
    }

    @PostMapping("/next")
    public PlaybackStateResponse next(CurrentUser user) {
        return playbackService.next(requireUser(user));
    }

    @PostMapping("/previous")
    public PlaybackStateResponse previous(CurrentUser user) {
        return playbackService.previous(requireUser(user));
    }

    @PostMapping("/queue")
    public QueueResponse enqueue(CurrentUser user, @Valid @RequestBody QueueTrackRequest request) {
        return playbackService.enqueue(requireUser(user), request.trackId());
    }

    @DeleteMapping("/queue/{trackId}")
    public QueueResponse removeFromQueue(CurrentUser user, @PathVariable UUID trackId) {
        return playbackService.removeFromQueue(requireUser(user), trackId);
    }

    @GetMapping("/queue")
    public QueueResponse queue(CurrentUser user) {
        return playbackService.queue(requireUser(user));
    }

    @PostMapping("/shuffle")
    public PlaybackStateResponse shuffle(CurrentUser user, @Valid @RequestBody ShuffleRequest request) {
        return playbackService.shuffle(requireUser(user), request.enabled());
    }

    @PostMapping("/repeat")
    public PlaybackStateResponse repeat(CurrentUser user, @Valid @RequestBody RepeatRequest request) {
        return playbackService.repeat(requireUser(user), request.mode());
    }

    @GetMapping("/state")
    public PlaybackStateResponse state(CurrentUser user) {
        return playbackService.state(requireUser(user));
    }

    @GetMapping("/stream-url")
    public StreamUrlResponse streamUrl(CurrentUser user) {
        return playbackService.streamUrl(requireUser(user));
    }

    private static UUID requireUser(CurrentUser user) {
        if (user == null || user.id() == null) {
            throw HarmoniaException.unauthorized(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        return user.id();
    }
}
