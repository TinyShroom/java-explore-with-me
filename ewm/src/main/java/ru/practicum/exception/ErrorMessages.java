package ru.practicum.exception;

import lombok.Getter;

@Getter
public enum ErrorMessages {

    USER_NOT_FOUND("user with id=%d not found"),
    CATEGORY_NOT_FOUND("category with id=%d not found"),
    EVENT_NOT_FOUND("event with id=%d not found"),
    EVENT_PUBLISHED("published event cannot be modified"),
    EVENT_START_SOON("event can be changed no later than two hours before the start"),
    EVENT_NOT_PUBLISHED("action is forbidden because the event is not published"),
    PARTICIPANT_LIMIT_REACHED("participant limit has been reached"),
    REQUESTER_IS_INITIATOR("initiator cannot submit a request to participate"),
    REQUEST_NOT_PENDING("only pending request can be modified"),
    EVENTS_NOT_FOUND("events with id: %s not found"),
    COMPILATION_NOT_FOUND("compilation with id=%d not found"),
    START_AFTER_END("start: %s is after end %s"),
    ALREADY_SUBSCRIBED("user with id=%s already subscribed to user with id=%s"),
    NOT_SUBSCRIBED("user with id=%s not subscribed to user with id=%s");

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

    public String getFormatMessage(String first, String second) {
        return String.format(message, first, second);
    }
}
