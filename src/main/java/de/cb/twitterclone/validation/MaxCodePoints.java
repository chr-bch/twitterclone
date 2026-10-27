package de.cb.twitterclone.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Counts code points instead of UTF-16 units like {@code @Size}, so an emoji counts as one. */
@Documented
@Constraint(validatedBy = MaxCodePointsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxCodePoints {

    int value();

    String message() default "must not exceed {value} characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
