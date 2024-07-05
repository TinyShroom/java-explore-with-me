package ru.practicum.events.constraint;

import ru.practicum.requests.model.RequestStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RejectedOrConfirmedValidator implements ConstraintValidator<RejectedOrConfirmed, RequestStatus> {

    @Override
    public boolean isValid(RequestStatus requestStatus, ConstraintValidatorContext constraintValidatorContext) {
        return RequestStatus.REJECTED == requestStatus || RequestStatus.CONFIRMED == requestStatus;
    }
}
