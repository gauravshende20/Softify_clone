package com.harmonia.user.web;

import com.harmonia.user.dto.PublicProfileResponse;
import com.harmonia.user.service.UserProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserProfileService profiles;

    public UserController(UserProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping("/{id}")
    public PublicProfileResponse publicProfile(@PathVariable UUID id) {
        return profiles.getPublicProfile(id);
    }
}
