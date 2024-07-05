package ru.practicum.events.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({ElementType.FIELD})
@Constraint(validatedBy = RejectedOrConfirmedValidator.class)
public @interface RejectedOrConfirmed {

    String message() default "status must be REJECTED or CONFIRMED";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default {};
}