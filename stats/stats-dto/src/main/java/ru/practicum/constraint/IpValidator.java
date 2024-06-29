package ru.practicum.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class IpValidator implements ConstraintValidator<Ip, String> {

    private static final Pattern PATTERN = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");

    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && PATTERN.matcher(value).matches();
    }
}