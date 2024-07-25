package ru.practicum.users.service;

import ru.practicum.users.dto.SubscriptionDto;

public interface SubscriptionService {

    SubscriptionDto subscribe(long userId, long subscriptionId);

    SubscriptionDto unsubscribe(long userId, long subscriptionId);

    SubscriptionDto getSubscriptions(long userId);
}
