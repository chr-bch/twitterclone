package de.cb.twitterclone.post;

import de.cb.twitterclone.user.AppUser;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "author")
    Window<Post> findAllByOrderByCreatedAtDescIdDesc(ScrollPosition position, Limit limit);

    @EntityGraph(attributePaths = "author")
    Window<Post> findByAuthorOrderByCreatedAtDescIdDesc(AppUser author, ScrollPosition position, Limit limit);

    @EntityGraph(attributePaths = "author")
    Optional<Post> findWithAuthorById(Long id);
}
