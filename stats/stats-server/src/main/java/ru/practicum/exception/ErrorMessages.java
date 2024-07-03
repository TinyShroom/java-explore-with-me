package ru.practicum.exception;

import lombok.Getter;

@Getter
public enum ErrorMessages {

    END_BEFORE_START("start must be before than end"),
    VALIDATION_EXCEPTION("Validation exception");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getFormatMessage(long arg) {
        return String.format(message, arg);
    }

    public String getFormatMessage(String arg) {
        return String.format(message, arg);
    }
}