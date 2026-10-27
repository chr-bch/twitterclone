package de.cb.twitterclone.post;

import de.cb.twitterclone.user.AppUser;
import de.cb.twitterclone.user.AppUserService;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PostService {

    public static final int MAX_LENGTH = 280;

    private final PostRepository posts;
    private final AppUserService users;

    public PostService(PostRepository posts, AppUserService users) {
        this.posts = posts;
        this.users = users;
    }

    @Transactional
    public PostResponse create(Jwt jwt, String content) {
        AppUser author = users.currentUser(jwt);
        // PostgreSQL stores microseconds; truncating keeps the returned value equal to the stored one.
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        return PostResponse.from(posts.save(new Post(author, content, now)));
    }

    @Transactional(readOnly = true)
    public PostResponse byId(long id) {
        return posts.findWithAuthorById(id)
                .map(PostResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post %d not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    public FeedResponse feed(String cursor, int limit) {
        return toResponse(posts.findAllByOrderByCreatedAtDescIdDesc(FeedCursor.toScrollPosition(cursor), Limit.of(limit)));
    }

    @Transactional(readOnly = true)
    public FeedResponse feedOf(String username, String cursor, int limit) {
        AppUser author = users.byUsername(username);
        return toResponse(posts.findByAuthorOrderByCreatedAtDescIdDesc(author, FeedCursor.toScrollPosition(cursor), Limit.of(limit)));
    }

    private static FeedResponse toResponse(Window<Post> window) {
        String nextCursor = window.hasNext() ? FeedCursor.of(window.getContent().getLast()).encode() : null;
        return new FeedResponse(window.map(PostResponse::from).getContent(), nextCursor);
    }
}
