package de.cb.twitterclone.post;

import org.springframework.data.domain.ScrollPosition;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Dto for the ScrollPosition
 */
record FeedCursor(Instant createdAt, long id) {

    private static final String SEPARATOR = "|";

    static FeedCursor of(Post post) {
        return new FeedCursor(post.getCreatedAt(), post.getId());
    }

    static ScrollPosition toScrollPosition(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return ScrollPosition.keyset();
        }
        FeedCursor cursor = decode(encoded);
        return ScrollPosition.forward(Map.of("createdAt", cursor.createdAt(), "id", cursor.id()));
    }

    String encode() {
        String raw = createdAt + SEPARATOR + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static FeedCursor decode(String encoded) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            int separator = raw.indexOf(SEPARATOR);
            return new FeedCursor(Instant.parse(raw.substring(0, separator)), Long.parseLong(raw.substring(separator + 1)));
        } catch (IllegalArgumentException | DateTimeException | IndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cursor", e);
        }
    }
}
