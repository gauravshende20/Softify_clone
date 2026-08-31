package com.harmonia.user.service;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.user.domain.FavoriteGenre;
import com.harmonia.user.domain.Profile;
import com.harmonia.user.domain.UserPreference;
import com.harmonia.user.dto.MeResponse;
import com.harmonia.user.dto.PreferenceResponse;
import com.harmonia.user.dto.ProfileResponse;
import com.harmonia.user.dto.PublicProfileResponse;
import com.harmonia.user.dto.UpdateFavoriteGenresRequest;
import com.harmonia.user.dto.UpdatePreferenceRequest;
import com.harmonia.user.dto.UpdateProfileRequest;
import com.harmonia.user.mapper.UserMapper;
import com.harmonia.user.repo.FavoriteGenreRepository;
import com.harmonia.user.repo.ProfileRepository;
import com.harmonia.user.repo.UserPreferenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserProfileService {

    private final ProfileRepository profiles;
    private final UserPreferenceRepository preferences;
    private final FavoriteGenreRepository favoriteGenres;
    private final UserMapper mapper;

    public UserProfileService(ProfileRepository profiles,
                              UserPreferenceRepository preferences,
                              FavoriteGenreRepository favoriteGenres,
                              UserMapper mapper) {
        this.profiles = profiles;
        this.preferences = preferences;
        this.favoriteGenres = favoriteGenres;
        this.mapper = mapper;
    }

    @Transactional
    public Profile createFromRegistration(UUID accountId, String email) {
        if (profiles.existsById(accountId)) {
            return profiles.getReferenceById(accountId);
        }
        Profile profile = Profile.create(accountId, displayNameFromEmail(email));
        profiles.save(profile);
        preferences.save(UserPreference.defaults(accountId));
        return profile;
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(UUID userId) {
        Profile profile = requireProfile(userId);
        UserPreference preference = preferences.findById(userId).orElseGet(() -> UserPreference.defaults(userId));
        return new MeResponse(toProfileResponse(profile), mapper.toPreference(preference));
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        Profile profile = requireProfile(userId);
        String country = request.country() == null ? null : request.country().toUpperCase(Locale.ROOT);
        profile.update(request.displayName(), request.avatarKey(), request.bio(), country);
        return toProfileResponse(profile);
    }

    @Transactional(readOnly = true)
    public PreferenceResponse getPreferences(UUID userId) {
        requireProfile(userId);
        UserPreference preference = preferences.findById(userId).orElseGet(() -> UserPreference.defaults(userId));
        return mapper.toPreference(preference);
    }

    @Transactional
    public PreferenceResponse updatePreferences(UUID userId, UpdatePreferenceRequest request) {
        requireProfile(userId);
        UserPreference preference = preferences.findById(userId).orElseGet(() -> {
            UserPreference created = UserPreference.defaults(userId);
            return preferences.save(created);
        });
        preference.update(request.locale(), request.explicitContent(), request.theme());
        return mapper.toPreference(preference);
    }

    @Transactional
    public ProfileResponse updateFavoriteGenres(UUID userId, UpdateFavoriteGenresRequest request) {
        requireProfile(userId);
        favoriteGenres.deleteByUserId(userId);
        List<UUID> unique = List.copyOf(new LinkedHashSet<>(request.genreIds()));
        unique.forEach(genreId -> favoriteGenres.save(new FavoriteGenre(userId, genreId)));
        return toProfileResponse(requireProfile(userId));
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(UUID userId) {
        return mapper.toPublicProfile(requireProfile(userId), genreIds(userId));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProfileResponse> listUsers(Pageable pageable) {
        Page<Profile> page = profiles.findAll(pageable);
        List<ProfileResponse> content = page.getContent().stream().map(this::toProfileResponse).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private Profile requireProfile(UUID userId) {
        return profiles.findById(userId)
                .orElseThrow(() -> HarmoniaException.notFound(ErrorCode.USER_NOT_FOUND, "User not found"));
    }

    private ProfileResponse toProfileResponse(Profile profile) {
        return mapper.toProfile(profile, genreIds(profile.getId()));
    }

    private List<UUID> genreIds(UUID userId) {
        return favoriteGenres.findByUserId(userId).stream().map(FavoriteGenre::getGenreId).toList();
    }

    static String displayNameFromEmail(String email) {
        if (email == null || email.isBlank()) {
            return "user";
        }
        int at = email.indexOf('@');
        String prefix = at > 0 ? email.substring(0, at) : email;
        return prefix.isBlank() ? "user" : prefix;
    }
}
