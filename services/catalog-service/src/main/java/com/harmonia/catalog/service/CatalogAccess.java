package com.harmonia.catalog.service;

import com.harmonia.catalog.domain.Artist;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.api.security.Roles;
import com.harmonia.common.security.CurrentUser;

final class CatalogAccess {

    private CatalogAccess() {
    }

    static boolean isStaff(CurrentUser user) {
        return user != null && (user.hasRole(Roles.ADMIN) || user.hasRole(Roles.MODERATOR));
    }

    static boolean isAdmin(CurrentUser user) {
        return user != null && user.hasRole(Roles.ADMIN);
    }

    static boolean isOwner(CurrentUser user, Artist artist) {
        return user != null && artist.getCreatedBy().equals(user.id());
    }

    static boolean canManage(CurrentUser user, Artist artist) {
        return isAdmin(user) || isOwner(user, artist);
    }

    static void requireManage(CurrentUser user, Artist artist) {
        if (!canManage(user, artist)) {
            throw HarmoniaException.forbidden(ErrorCode.FORBIDDEN, "You cannot manage this artist");
        }
    }

    static void requireStaff(CurrentUser user) {
        if (!isStaff(user)) {
            throw HarmoniaException.forbidden(ErrorCode.FORBIDDEN, "Moderator or admin role required");
        }
    }
}
