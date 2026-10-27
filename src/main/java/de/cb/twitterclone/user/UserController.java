package de.cb.twitterclone.user;

import de.cb.twitterclone.web.OpenApiConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserService users;

    public UserController(AppUserService users) {
        this.users = users;
    }

    @GetMapping("/me")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(users.currentUser(jwt));
    }

    @GetMapping("/{username}")
    public UserResponse byUsername(@PathVariable String username) {
        return UserResponse.from(users.byUsername(username));
    }
}
