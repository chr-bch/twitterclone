package de.cb.twitterclone.post;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreatePostRequestValidationTest {

    private static final String EMOJI = "😀";

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void acceptsExactly280AsciiCharacters() {
        assertThat(validate("a".repeat(280))).isEmpty();
    }

    @Test
    void rejects281AsciiCharacters() {
        assertThat(validate("a".repeat(281))).singleElement()
                .satisfies(violation -> assertThat(violation.getMessage()).isEqualTo("must not exceed 280 characters"));
    }

    @Test
    void counts280EmojisAs280CharactersAlthoughJavaSeesTwiceAsMany() {
        String content = EMOJI.repeat(280);

        assertThat(content.length()).isEqualTo(560);
        assertThat(validate(content)).isEmpty();
    }

    @Test
    void rejects281Emojis() {
        assertThat(validate(EMOJI.repeat(281))).hasSize(1);
    }

    @Test
    void countsMixedContentByCodePoints() {
        assertThat(validate("a".repeat(279) + EMOJI)).isEmpty();
        assertThat(validate("a".repeat(280) + EMOJI)).hasSize(1);
    }

    @Test
    void rejectsBlankContent() {
        assertThat(validate("   ")).hasSize(1);
        assertThat(validate(null)).hasSize(1);
    }

    private static Set<ConstraintViolation<CreatePostRequest>> validate(String content) {
        return validator.validate(new CreatePostRequest(content));
    }
}
