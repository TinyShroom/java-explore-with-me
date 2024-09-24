package ru.practicum.users.dao;

import ru.practicum.users.dto.SubscriptionDto;

import java.util.Optional;

public interface SubscriptionsStorage {

    Optional<SubscriptionDto> getSubscriptions(long userId);
}
