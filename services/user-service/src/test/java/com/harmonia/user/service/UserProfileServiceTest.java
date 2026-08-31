package com.harmonia.user.service;

import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.user.domain.FavoriteGenre;
import com.harmonia.user.domain.Profile;
import com.harmonia.user.domain.UserPreference;
import com.harmonia.user.dto.MeResponse;
import com.harmonia.user.dto.UpdateFavoriteGenresRequest;
import com.harmonia.user.dto.UpdateProfileRequest;
import com.harmonia.user.mapper.UserMapper;
import com.harmonia.user.repo.FavoriteGenreRepository;
import com.harmonia.user.repo.ProfileRepository;
import com.harmonia.user.repo.UserPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock ProfileRepository profiles;
    @Mock UserPreferenceRepository preferences;
    @Mock FavoriteGenreRepository favoriteGenres;

    UserProfileService service;

    @BeforeEach
    void setUp() {
        service = new UserProfileService(profiles, preferences, favoriteGenres, Mappers.getMapper(UserMapper.class));
    }

    @Test
    void displayNameFromEmailUsesPrefix() {
        assertEquals("ada", UserProfileService.displayNameFromEmail("ada@harmonia.local"));
        assertEquals("user", UserProfileService.displayNameFromEmail(""));
    }

    @Test
    void createFromRegistrationIsIdempotent() {
        UUID id = UUID.randomUUID();
        when(profiles.existsById(id)).thenReturn(true);
        when(profiles.getReferenceById(id)).thenReturn(Profile.create(id, "ada"));
        service.createFromRegistration(id, "ada@harmonia.local");
        verify(profiles, never()).save(any());
    }

    @Test
    void createFromRegistrationPersistsProfileAndDefaults() {
        UUID id = UUID.randomUUID();
        when(profiles.existsById(id)).thenReturn(false);
        when(profiles.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        Profile created = service.createFromRegistration(id, "ada@harmonia.local");
        assertEquals("ada", created.getDisplayName());
        verify(preferences).save(any(UserPreference.class));
    }

    @Test
    void getMeThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(profiles.findById(id)).thenReturn(Optional.empty());
        HarmoniaException ex = assertThrows(HarmoniaException.class, () -> service.getMe(id));
        assertEquals(404, ex.getStatus());
    }

    @Test
    void updateProfileAndGenres() {
        UUID id = UUID.randomUUID();
        Profile profile = Profile.create(id, "ada");
        when(profiles.findById(id)).thenReturn(Optional.of(profile));
        UUID genre = UUID.randomUUID();
        when(favoriteGenres.findByUserId(id)).thenReturn(List.of(new FavoriteGenre(id, genre)));

        service.updateProfile(id, new UpdateProfileRequest("Ada Lovelace", null, "Composer", "gb"));
        assertEquals("Ada Lovelace", profile.getDisplayName());
        assertEquals("GB", profile.getCountry());

        service.updateFavoriteGenres(id, new UpdateFavoriteGenresRequest(List.of(genre)));
        verify(favoriteGenres).deleteByUserId(id);
        verify(favoriteGenres).save(any(FavoriteGenre.class));
    }

    @Test
    void getMeReturnsPreferences() {
        UUID id = UUID.randomUUID();
        when(profiles.findById(id)).thenReturn(Optional.of(Profile.create(id, "ada")));
        when(preferences.findById(id)).thenReturn(Optional.of(UserPreference.defaults(id)));
        when(favoriteGenres.findByUserId(id)).thenReturn(List.of());
        MeResponse me = service.getMe(id);
        assertEquals("ada", me.profile().displayName());
        assertEquals("dark", me.preferences().theme());
    }
}
