package com.harmonia.user.web;

import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.common.security.CurrentUser;
import com.harmonia.user.dto.FollowedArtistResponse;
import com.harmonia.user.dto.LikedTrackResponse;
import com.harmonia.user.service.SocialService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/library")
public class LibraryController {

    private final SocialService social;

    public LibraryController(SocialService social) {
        this.social = social;
    }

    @GetMapping("/liked-songs")
    public PageResponse<LikedTrackResponse> likedSongs(CurrentUser user,
                                                       @PageableDefault(size = 50) Pageable pageable) {
        return social.likedTracks(user.id(), pageable);
    }

    @PostMapping("/liked-songs/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(CurrentUser user, @PathVariable UUID trackId) {
        social.likeTrack(user.id(), trackId);
    }

    @DeleteMapping("/liked-songs/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(CurrentUser user, @PathVariable UUID trackId) {
        social.unlikeTrack(user.id(), trackId);
    }

    @GetMapping("/artists")
    public List<FollowedArtistResponse> artists(CurrentUser user) {
        return social.followedArtists(user.id());
    }

    @PostMapping("/artists/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(CurrentUser user, @PathVariable UUID artistId) {
        social.followArtist(user.id(), artistId);
    }

    @DeleteMapping("/artists/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(CurrentUser user, @PathVariable UUID artistId) {
        social.unfollowArtist(user.id(), artistId);
    }
}
