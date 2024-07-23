package ru.practicum.users.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.logging.Logging;
import ru.practicum.users.dto.UserSubscriptionDto;
import ru.practicum.users.service.UserService;

@RestController
@RequestMapping(path = "/users/{userId}/subscriptions")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Logging
    @GetMapping
    public UserSubscriptionDto get(@PathVariable long userId) {
        return userService.getSubscriptions(userId);
    }

    @Logging
    @PatchMapping("/{subscriptionId}")
    public UserSubscriptionDto subscribe(@PathVariable long userId, @PathVariable long subscriptionId) {
        return userService.subscribe(userId, subscriptionId);
    }

    @Logging
    @PatchMapping("/{subscriptionId}/unsubscribe")
    public UserSubscriptionDto unsubscribe(@PathVariable long userId, @PathVariable long subscriptionId) {
        return userService.unsubscribe(userId, subscriptionId);
    }
}
