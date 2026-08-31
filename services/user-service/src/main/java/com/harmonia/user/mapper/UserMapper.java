package com.harmonia.user.mapper;

import com.harmonia.user.domain.FollowedArtist;
import com.harmonia.user.domain.LikedTrack;
import com.harmonia.user.domain.Profile;
import com.harmonia.user.domain.RecentlyPlayed;
import com.harmonia.user.domain.UserPreference;
import com.harmonia.user.dto.FollowedArtistResponse;
import com.harmonia.user.dto.LikedTrackResponse;
import com.harmonia.user.dto.PreferenceResponse;
import com.harmonia.user.dto.ProfileResponse;
import com.harmonia.user.dto.PublicProfileResponse;
import com.harmonia.user.dto.RecentlyPlayedResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "favoriteGenreIds", source = "genreIds")
    ProfileResponse toProfile(Profile profile, List<UUID> genreIds);

    @Mapping(target = "favoriteGenreIds", source = "genreIds")
    PublicProfileResponse toPublicProfile(Profile profile, List<UUID> genreIds);

    PreferenceResponse toPreference(UserPreference preference);

    LikedTrackResponse toLikedTrack(LikedTrack liked);

    FollowedArtistResponse toFollowedArtist(FollowedArtist followed);

    RecentlyPlayedResponse toRecentlyPlayed(RecentlyPlayed played);
}
