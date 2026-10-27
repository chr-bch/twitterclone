package de.cb.twitterclone.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class AppUserService {

    private final AppUserRepository users;

    public AppUserService(AppUserRepository users) {
        this.users = users;
    }

    /**
     * Retrieves the {@link AppUser}. Creates it from the claims, if it doesn't exist.
     */
    @Transactional
    public AppUser currentUser(Jwt jwt) {
        String externalId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String displayName = jwt.hasClaim("name") ? jwt.getClaimAsString("name") : username;

        return users.findByExternalId(externalId)
                .map(user -> {
                    user.updateProfile(username, displayName);
                    return user;
                })
                .orElseGet(() -> users.save(new AppUser(externalId, username, displayName, Instant.now())));
    }

    @Transactional(readOnly = true)
    public AppUser byUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User '%s' not found".formatted(username)));
    }
}
