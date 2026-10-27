package de.cb.twitterclone.post;

import de.cb.twitterclone.validation.MaxCodePoints;
import jakarta.validation.constraints.NotBlank;

/**
 * A post of an {@link de.cb.twitterclone.user.AppUser}.
 * @param content The content of a post.
 */
public record CreatePostRequest(
        @NotBlank
        @MaxCodePoints(PostService.MAX_LENGTH)
        String content) {
}
