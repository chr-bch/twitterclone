package de.cb.twitterclone.user;

import java.time.Instant;

public record UserResponse(String username, String displayName, Instant memberSince) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getUsername(), user.getDisplayName(), user.getCreatedAt());
    }
}
