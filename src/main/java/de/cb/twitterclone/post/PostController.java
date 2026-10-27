package de.cb.twitterclone.post;

import de.cb.twitterclone.web.OpenApiConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class PostController {

    private static final String DEFAULT_PAGE_SIZE = "20";
    private static final int MAX_PAGE_SIZE = 100;

    private final PostService posts;

    public PostController(PostService posts) {
        this.posts = posts;
    }

    @GetMapping("/posts")
    public FeedResponse feed(@RequestParam(required = false) String cursor,
                             @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int limit) {
        return posts.feed(cursor, limit);
    }

    @GetMapping("/users/{username}/posts")
    public FeedResponse feedOf(@PathVariable String username,
                               @RequestParam(required = false) String cursor,
                               @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int limit) {
        return posts.feedOf(username, cursor, limit);
    }

    @GetMapping("/posts/{id}")
    public PostResponse byId(@PathVariable long id) {
        return posts.byId(id);
    }

    @PostMapping("/posts")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    public ResponseEntity<PostResponse> create(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody CreatePostRequest request) {
        PostResponse created = posts.create(jwt, request.content());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
