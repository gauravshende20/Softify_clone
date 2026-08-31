package com.harmonia.user.service;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.DomainEventPublisher;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.user.domain.FollowedArtist;
import com.harmonia.user.domain.LikedTrack;
import com.harmonia.user.domain.RecentlyPlayed;
import com.harmonia.user.dto.FollowedArtistResponse;
import com.harmonia.user.dto.LikedTrackResponse;
import com.harmonia.user.dto.RecentlyPlayedResponse;
import com.harmonia.user.mapper.UserMapper;
import com.harmonia.user.repo.FollowedArtistRepository;
import com.harmonia.user.repo.LikedTrackRepository;
import com.harmonia.user.repo.ProfileRepository;
import com.harmonia.user.repo.RecentlyPlayedRepository;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SocialService {

    public static final int RECENTLY_PLAYED_CAP = 100;
    private static final String PRODUCER = "user-service";

    private final ProfileRepository profiles;
    private final LikedTrackRepository likedTracks;
    private final FollowedArtistRepository followedArtists;
    private final RecentlyPlayedRepository recentlyPlayed;
    private final DomainEventPublisher events;
    private final UserMapper mapper;

    public SocialService(ProfileRepository profiles,
                         LikedTrackRepository likedTracks,
                         FollowedArtistRepository followedArtists,
                         RecentlyPlayedRepository recentlyPlayed,
                         DomainEventPublisher events,
                         UserMapper mapper) {
        this.profiles = profiles;
        this.likedTracks = likedTracks;
        this.followedArtists = followedArtists;
        this.recentlyPlayed = recentlyPlayed;
        this.events = events;
        this.mapper = mapper;
    }

    @Transactional
    public void likeTrack(UUID userId, UUID trackId) {
        requireProfile(userId);
        if (likedTracks.existsByUserIdAndTrackId(userId, trackId)) {
            return;
        }
        likedTracks.save(new LikedTrack(userId, trackId));
        publish(Topics.SOCIAL, EventType.TRACK_LIKED, "Track", trackId, userId,
                Map.of("userId", userId.toString(), "trackId", trackId.toString()));
    }

    @Transactional
    public void unlikeTrack(UUID userId, UUID trackId) {
        requireProfile(userId);
        if (!likedTracks.existsByUserIdAndTrackId(userId, trackId)) {
            return;
        }
        likedTracks.deleteByUserIdAndTrackId(userId, trackId);
        publish(Topics.SOCIAL, EventType.TRACK_UNLIKED, "Track", trackId, userId,
                Map.of("userId", userId.toString(), "trackId", trackId.toString()));
    }

    @Transactional(readOnly = true)
    public PageResponse<LikedTrackResponse> likedTracks(UUID userId, Pageable pageable) {
        requireProfile(userId);
        Page<LikedTrack> page = likedTracks.findByUserIdOrderByLikedAtDesc(userId, pageable);
        return PageResponse.of(
                page.getContent().stream().map(mapper::toLikedTrack).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional
    public void followArtist(UUID userId, UUID artistId) {
        requireProfile(userId);
        if (followedArtists.existsByUserIdAndArtistId(userId, artistId)) {
            return;
        }
        followedArtists.save(new FollowedArtist(userId, artistId));
        publish(Topics.SOCIAL, EventType.ARTIST_FOLLOWED, "Artist", artistId, userId,
                Map.of("userId", userId.toString(), "artistId", artistId.toString()));
    }

    @Transactional
    public void unfollowArtist(UUID userId, UUID artistId) {
        requireProfile(userId);
        if (!followedArtists.existsByUserIdAndArtistId(userId, artistId)) {
            return;
        }
        followedArtists.deleteByUserIdAndArtistId(userId, artistId);
        publish(Topics.SOCIAL, EventType.ARTIST_UNFOLLOWED, "Artist", artistId, userId,
                Map.of("userId", userId.toString(), "artistId", artistId.toString()));
    }

    @Transactional(readOnly = true)
    public List<FollowedArtistResponse> followedArtists(UUID userId) {
        requireProfile(userId);
        return followedArtists.findByUserIdOrderByFollowedAtDesc(userId).stream()
                .map(mapper::toFollowedArtist)
                .toList();
    }

    @Transactional
    public void recordPlayback(UUID userId, UUID trackId) {
        if (!profiles.existsById(userId)) {
            return;
        }
        recentlyPlayed.save(RecentlyPlayed.create(userId, trackId));
        long count = recentlyPlayed.countByUserId(userId);
        if (count > RECENTLY_PLAYED_CAP) {
            int overflow = (int) (count - RECENTLY_PLAYED_CAP);
            List<RecentlyPlayed> oldest = recentlyPlayed.findByUserIdOrderByPlayedAtAsc(
                    userId, PageRequest.of(0, overflow));
            recentlyPlayed.deleteAll(oldest);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<RecentlyPlayedResponse> recentlyPlayed(UUID userId, Pageable pageable) {
        requireProfile(userId);
        Page<RecentlyPlayed> page = recentlyPlayed.findByUserIdOrderByPlayedAtDesc(userId, pageable);
        return PageResponse.of(
                page.getContent().stream().map(mapper::toRecentlyPlayed).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private void requireProfile(UUID userId) {
        if (!profiles.existsById(userId)) {
            throw HarmoniaException.notFound(ErrorCode.USER_NOT_FOUND, "User not found");
        }
    }

    private void publish(String topic, EventType type, String aggregateType, UUID aggregateId,
                         UUID userId, Map<String, Object> payload) {
        events.publish(topic, DomainEvent.of(
                type, aggregateType, aggregateId.toString(),
                PRODUCER, MDC.get("traceId"), userId.toString(), payload));
    }
}
