package com.harmonia.user.service;

import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.DomainEventPublisher;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.user.domain.FollowedArtist;
import com.harmonia.user.domain.LikedTrack;
import com.harmonia.user.domain.RecentlyPlayed;
import com.harmonia.user.mapper.UserMapper;
import com.harmonia.user.repo.FollowedArtistRepository;
import com.harmonia.user.repo.LikedTrackRepository;
import com.harmonia.user.repo.ProfileRepository;
import com.harmonia.user.repo.RecentlyPlayedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialServiceTest {

    @Mock ProfileRepository profiles;
    @Mock LikedTrackRepository likedTracks;
    @Mock FollowedArtistRepository followedArtists;
    @Mock RecentlyPlayedRepository recentlyPlayed;
    @Mock DomainEventPublisher events;

    SocialService service;

    @BeforeEach
    void setUp() {
        service = new SocialService(profiles, likedTracks, followedArtists, recentlyPlayed, events,
                Mappers.getMapper(UserMapper.class));
    }

    @Test
    void likeTrackPublishesOnce() {
        UUID user = UUID.randomUUID();
        UUID track = UUID.randomUUID();
        when(profiles.existsById(user)).thenReturn(true);
        when(likedTracks.existsByUserIdAndTrackId(user, track)).thenReturn(false);
        service.likeTrack(user, track);
        verify(likedTracks).save(any(LikedTrack.class));
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(events).publish(eq(Topics.SOCIAL), captor.capture());
        assertEquals(EventType.TRACK_LIKED.name(), captor.getValue().eventType());
    }

    @Test
    void likeTrackIsIdempotent() {
        UUID user = UUID.randomUUID();
        UUID track = UUID.randomUUID();
        when(profiles.existsById(user)).thenReturn(true);
        when(likedTracks.existsByUserIdAndTrackId(user, track)).thenReturn(true);
        service.likeTrack(user, track);
        verify(likedTracks, never()).save(any());
        verify(events, never()).publish(any(), any());
    }

    @Test
    void unlikeTrackPublishesUnliked() {
        UUID user = UUID.randomUUID();
        UUID track = UUID.randomUUID();
        when(profiles.existsById(user)).thenReturn(true);
        when(likedTracks.existsByUserIdAndTrackId(user, track)).thenReturn(true);
        service.unlikeTrack(user, track);
        verify(likedTracks).deleteByUserIdAndTrackId(user, track);
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(events).publish(eq(Topics.SOCIAL), captor.capture());
        assertEquals(EventType.TRACK_UNLIKED.name(), captor.getValue().eventType());
    }

    @Test
    void followAndUnfollowPublishSocialEvents() {
        UUID user = UUID.randomUUID();
        UUID artist = UUID.randomUUID();
        when(profiles.existsById(user)).thenReturn(true);
        when(followedArtists.existsByUserIdAndArtistId(user, artist)).thenReturn(false);
        service.followArtist(user, artist);
        verify(followedArtists).save(any(FollowedArtist.class));

        when(followedArtists.existsByUserIdAndArtistId(user, artist)).thenReturn(true);
        service.unfollowArtist(user, artist);
        verify(followedArtists).deleteByUserIdAndArtistId(user, artist);
        verify(events, org.mockito.Mockito.times(2)).publish(eq(Topics.SOCIAL), any(DomainEvent.class));
    }

    @Test
    void recordPlaybackCapsAtOneHundred() {
        UUID user = UUID.randomUUID();
        UUID track = UUID.randomUUID();
        when(profiles.existsById(user)).thenReturn(true);
        when(recentlyPlayed.countByUserId(user)).thenReturn(101L);
        List<RecentlyPlayed> oldest = IntStream.range(0, 1).mapToObj(i -> RecentlyPlayed.create(user, track)).toList();
        when(recentlyPlayed.findByUserIdOrderByPlayedAtAsc(user, PageRequest.of(0, 1))).thenReturn(oldest);
        service.recordPlayback(user, track);
        verify(recentlyPlayed).save(any(RecentlyPlayed.class));
        verify(recentlyPlayed).deleteAll(oldest);
    }
}
