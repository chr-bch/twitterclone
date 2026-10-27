package de.cb.twitterclone.post;

import com.jayway.jsonpath.JsonPath;
import de.cb.twitterclone.TestcontainersConfiguration;
import de.cb.twitterclone.user.AppUser;
import de.cb.twitterclone.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PostApiIntegrationTest {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private PostRepository posts;

    @Autowired
    private AppUserRepository users;

    @Autowired
    private JdbcClient jdbc;

    @BeforeEach
    void cleanDatabase() {
        posts.deleteAll();
        users.deleteAll();
    }

    @Test
    void createsUserProfileOnFirstPost() {
        createPost(as("alice"), "{\"content\":\"first\"}");
        createPost(as("alice"), "{\"content\":\"second\"}");

        assertThat(users.findAll()).singleElement()
                .satisfies(user -> assertThat(user.getUsername()).isEqualTo("alice"));
        assertThat(mvc.get().uri("/api/users/alice")).hasStatusOk()
                .bodyJson().extractingPath("$.displayName").isEqualTo("Alice Example");
    }

    @Test
    void feedReturnsNewestFirstAndPagesWithCursor() throws Exception {
        for (String content : List.of("one", "two", "three")) {
            createPost(as("alice"), "{\"content\":\"" + content + "\"}");
        }

        MvcTestResult firstPage = mvc.get().uri("/api/posts?limit=2").exchange();
        assertThat(firstPage).bodyJson().extractingPath("$.posts[*].content").isEqualTo(List.of("three", "two"));

        String cursor = JsonPath.read(body(firstPage), "$.nextCursor");
        MvcTestResult secondPage = mvc.get().uri("/api/posts?limit=2&cursor={cursor}", cursor).exchange();
        assertThat(secondPage).bodyJson().extractingPath("$.posts[*].content").isEqualTo(List.of("one"));
        assertThat(secondPage).bodyJson().extractingPath("$.nextCursor").isNull();
    }

    @Test
    void usesIdAsTiebreakerForEqualTimestamps() throws Exception {
        AppUser alice = users.save(new AppUser("sub-alice", "alice", "Alice", Instant.now()));
        Instant sameTime = Instant.parse("2026-10-25T12:00:00Z");
        Post first = posts.save(new Post(alice, "first", sameTime));
        Post second = posts.save(new Post(alice, "second", sameTime));
        Post third = posts.save(new Post(alice, "third", sameTime));

        MvcTestResult firstPage = mvc.get().uri("/api/posts?limit=2").exchange();
        assertThat(firstPage).bodyJson().extractingPath("$.posts[*].id")
                .isEqualTo(List.of(third.getId().intValue(), second.getId().intValue()));

        String cursor = JsonPath.read(body(firstPage), "$.nextCursor");
        assertThat(mvc.get().uri("/api/posts?limit=2&cursor={cursor}", cursor))
                .bodyJson().extractingPath("$.posts[*].id").isEqualTo(List.of(first.getId().intValue()));
    }

    @Test
    void newPostsDoNotShiftFollowingPages() throws Exception {
        createPost(as("alice"), "{\"content\":\"one\"}");
        createPost(as("alice"), "{\"content\":\"two\"}");

        MvcTestResult firstPage = mvc.get().uri("/api/posts?limit=1").exchange();
        String cursor = JsonPath.read(body(firstPage), "$.nextCursor");

        createPost(as("bob"), "{\"content\":\"brand new\"}");

        assertThat(mvc.get().uri("/api/posts?limit=1&cursor={cursor}", cursor))
                .bodyJson().extractingPath("$.posts[*].content").isEqualTo(List.of("one"));
    }

    @Test
    void userFeedOnlyContainsPostsOfThatUser() {
        createPost(as("alice"), "{\"content\":\"from alice\"}");
        createPost(as("bob"), "{\"content\":\"from bob\"}");

        assertThat(mvc.get().uri("/api/users/bob/posts"))
                .bodyJson().extractingPath("$.posts[*].content").isEqualTo(List.of("from bob"));
        assertThat(mvc.get().uri("/api/users/nobody/posts")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void storesEmojisUpToTheLimit() {
        String content = "😀".repeat(280);

        assertThat(createPost(as("alice"), "{\"content\":\"" + content + "\"}"))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson().extractingPath("$.content").isEqualTo(content);
        assertThat(jdbc.sql("SELECT char_length(content) FROM post").query(Integer.class).single()).isEqualTo(280);
    }

    @Test
    void databaseRejectsContentAboveLimitEvenWithoutApiValidation() {
        AppUser alice = users.save(new AppUser("sub-alice", "alice", "Alice", Instant.now()));

        assertThatThrownBy(() -> posts.saveAndFlush(new Post(alice, "😀".repeat(281), Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("post_content_length");
    }

    @Test
    void rejectsInvalidCursor() {
        assertThat(mvc.get().uri("/api/posts?cursor=not-a-cursor"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    private MvcTestResult createPost(RequestPostProcessor user, String json) {
        return mvc.post().uri("/api/posts")
                .with(user)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private static RequestPostProcessor as(String username) {
        String displayName = Character.toUpperCase(username.charAt(0)) + username.substring(1) + " Example";
        return jwt().jwt(token -> token
                .subject("sub-" + username)
                .claim("preferred_username", username)
                .claim("name", displayName));
    }

    private static String body(MvcTestResult result) throws UnsupportedEncodingException {
        return result.getResponse().getContentAsString();
    }
}
