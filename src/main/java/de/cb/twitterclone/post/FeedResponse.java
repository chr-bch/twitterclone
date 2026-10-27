package de.cb.twitterclone.post;

import java.util.List;

public record FeedResponse(List<PostResponse> posts, String nextCursor) {
}
