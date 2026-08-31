package com.harmonia.user.web;

import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.user.dto.ProfileResponse;
import com.harmonia.user.service.UserProfileService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserProfileService profiles;

    public AdminUserController(UserProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<ProfileResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return profiles.listUsers(pageable);
    }
}
