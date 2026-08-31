package com.harmonia.user.web;

import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.common.security.CurrentUser;
import com.harmonia.user.dto.FollowedArtistResponse;
import com.harmonia.user.dto.LikedTrackResponse;
import com.harmonia.user.dto.MeResponse;
import com.harmonia.user.dto.PreferenceResponse;
import com.harmonia.user.dto.ProfileResponse;
import com.harmonia.user.dto.RecentlyPlayedResponse;
import com.harmonia.user.dto.UpdateFavoriteGenresRequest;
import com.harmonia.user.dto.UpdatePreferenceRequest;
import com.harmonia.user.dto.UpdateProfileRequest;
import com.harmonia.user.service.SocialService;
import com.harmonia.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final UserProfileService profiles;
    private final SocialService social;

    public MeController(UserProfileService profiles, SocialService social) {
        this.profiles = profiles;
        this.social = social;
    }

    @GetMapping
    public MeResponse me(CurrentUser user) {
        return profiles.getMe(user.id());
    }

    @PutMapping
    public ProfileResponse updateProfile(CurrentUser user, @Valid @RequestBody UpdateProfileRequest request) {
        return profiles.updateProfile(user.id(), request);
    }

    @GetMapping("/preferences")
    public PreferenceResponse preferences(CurrentUser user) {
        return profiles.getPreferences(user.id());
    }

    @PutMapping("/preferences")
    public PreferenceResponse updatePreferences(CurrentUser user, @Valid @RequestBody UpdatePreferenceRequest request) {
        return profiles.updatePreferences(user.id(), request);
    }

    @PutMapping("/genres")
    public ProfileResponse updateGenres(CurrentUser user, @Valid @RequestBody UpdateFavoriteGenresRequest request) {
        return profiles.updateFavoriteGenres(user.id(), request);
    }

    @PostMapping("/likes/tracks/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeTrack(CurrentUser user, @PathVariable UUID trackId) {
        social.likeTrack(user.id(), trackId);
    }

    @DeleteMapping("/likes/tracks/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikeTrack(CurrentUser user, @PathVariable UUID trackId) {
        social.unlikeTrack(user.id(), trackId);
    }

    @GetMapping("/likes/tracks")
    public PageResponse<LikedTrackResponse> likedTracks(CurrentUser user,
                                                        @PageableDefault(size = 20) Pageable pageable) {
        return social.likedTracks(user.id(), pageable);
    }

    @PostMapping("/follows/artists/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void followArtist(CurrentUser user, @PathVariable UUID artistId) {
        social.followArtist(user.id(), artistId);
    }

    @DeleteMapping("/follows/artists/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollowArtist(CurrentUser user, @PathVariable UUID artistId) {
        social.unfollowArtist(user.id(), artistId);
    }

    @GetMapping("/follows/artists")
    public List<FollowedArtistResponse> followedArtists(CurrentUser user) {
        return social.followedArtists(user.id());
    }

    @GetMapping("/recently-played")
    public PageResponse<RecentlyPlayedResponse> recentlyPlayed(CurrentUser user,
                                                               @PageableDefault(size = 20) Pageable pageable) {
        return social.recentlyPlayed(user.id(), pageable);
    }
}
