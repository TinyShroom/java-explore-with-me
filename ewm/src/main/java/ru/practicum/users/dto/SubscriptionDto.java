package ru.practicum.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {

    private long id;
    private String name;
    private String email;
    private Set<UserShortDto> subscriptions = new HashSet<>();
}
