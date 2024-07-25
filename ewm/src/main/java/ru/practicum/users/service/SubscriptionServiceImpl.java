package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;
import ru.practicum.users.dao.SubscriptionsStorage;
import ru.practicum.users.dao.UserRepository;
import ru.practicum.users.dto.SubscriptionDto;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserRepository userRepository;
    private final SubscriptionsStorage subscriptionsStorage;

    @Override
    @Transactional
    public SubscriptionDto subscribe(long userId, long subscriptionId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId));
        }
        if (!userRepository.existsById(subscriptionId)) {
            throw new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(subscriptionId));
        }
        userRepository.addSubscription(userId, subscriptionId);
        return subscriptionsStorage.getSubscriptions(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
    }

    @Override
    @Transactional
    public SubscriptionDto unsubscribe(long userId, long subscriptionId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId));
        }
        userRepository.deleteSubscription(userId, subscriptionId);
        return subscriptionsStorage.getSubscriptions(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDto getSubscriptions(long userId) {
        return subscriptionsStorage.getSubscriptions(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
    }
}
