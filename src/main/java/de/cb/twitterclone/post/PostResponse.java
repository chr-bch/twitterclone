package de.cb.twitterclone.post;

import java.time.Instant;

public record PostResponse(Long id, Author author, String content, Instant createdAt) {

    public record Author(String username, String displayName) {
    }

    static PostResponse from(Post post) {
        var author = post.getAuthor();
        return new PostResponse(
                post.getId(),
                new Author(author.getUsername(), author.getDisplayName()),
                post.getContent(),
                post.getCreatedAt());
    }
}
