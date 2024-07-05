package ru.practicum.events.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class AfterCurrentTimeValidator implements ConstraintValidator<AfterCurrentTime, LocalDateTime> {

    private int hours = 0;

    @Override
    public void initialize(AfterCurrentTime annotation) {
        try {
            hours = Integer.parseInt(annotation.hours());
        } catch (Exception e) {
            hours = 0;
        }
    }

    public boolean isValid(LocalDateTime value, ConstraintValidatorContext constraintValidatorContext) {
        return value == null || hours == 0 || value.isAfter(LocalDateTime.now().plusHours(hours));
    }
}