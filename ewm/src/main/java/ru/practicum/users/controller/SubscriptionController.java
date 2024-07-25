package ru.practicum.users.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.logging.Logging;
import ru.practicum.users.dto.SubscriptionDto;
import ru.practicum.users.service.SubscriptionService;

@RestController
@RequestMapping(path = "/users/{userId}/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Logging
    @GetMapping
    public SubscriptionDto get(@PathVariable long userId) {
        return subscriptionService.getSubscriptions(userId);
    }

    @Logging
    @PatchMapping("/{subscriptionId}")
    public SubscriptionDto subscribe(@PathVariable long userId, @PathVariable long subscriptionId) {
        return subscriptionService.subscribe(userId, subscriptionId);
    }

    @Logging
    @PatchMapping("/{subscriptionId}/unsubscribe")
    public SubscriptionDto unsubscribe(@PathVariable long userId, @PathVariable long subscriptionId) {
        return subscriptionService.unsubscribe(userId, subscriptionId);
    }
}
