package de.cb.twitterclone.post;

import de.cb.twitterclone.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
class PostControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void feedIsPublic() {
        given(postService.feed(null, 20)).willReturn(new FeedResponse(List.of(), null));

        assertThat(mvc.get().uri("/api/posts")).hasStatusOk();
    }

    @Test
    void rejectsPageSizeAboveMaximum() {
        assertThat(mvc.get().uri("/api/posts?limit=101"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void creatingRequiresAuthentication() {
        assertThat(mvc.post().uri("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"hello\"}"))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .satisfies(result -> assertThat(result.getResponse().getHeader("WWW-Authenticate")).startsWith("Bearer"))
                .bodyJson().extractingPath("$.status").isEqualTo(401);

        verifyNoInteractions(postService);
    }

    @Test
    void createsPostForAuthenticatedUser() {
        var created = new PostResponse(42L, new PostResponse.Author("clone", "Clone User"), "hello", Instant.parse("2026-10-25T12:00:00Z"));
        given(postService.create(any(Jwt.class), eq("hello"))).willReturn(created);

        assertThat(mvc.post().uri("/api/posts")
                .with(jwt().jwt(token -> token.subject("user-1").claim("preferred_username", "clone")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"hello\"}"))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "http://localhost/api/posts/42")
                .bodyJson().extractingPath("$.author.username").isEqualTo("clone");
    }

    @Test
    void returnsValidationErrorsAsProblemDetail() {
        assertThat(mvc.post().uri("/api/posts")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"" + "a".repeat(281) + "\"}"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.errors.content").isEqualTo("must not exceed 280 characters");

        verifyNoInteractions(postService);
    }
}
