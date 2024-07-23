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
@Constraint(validatedBy = AfterCurrentTimeValidator.class)
public @interface AfterCurrentTime {

    String hours() default "0";

    String message() default "time must be later than current";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default {};
}