package ru.practicum.users.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserSubscriptionDto {

    private long id;
    private String name;
    private String email;
    private Set<UserShortDto> subscriptions = new HashSet<>();
}
