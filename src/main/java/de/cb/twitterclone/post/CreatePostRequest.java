package de.cb.twitterclone.post;

import de.cb.twitterclone.validation.MaxCodePoints;
import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank
        @MaxCodePoints(PostService.MAX_LENGTH)
        String content) {
}
