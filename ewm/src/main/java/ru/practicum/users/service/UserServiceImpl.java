package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;
import ru.practicum.users.dao.UserRepository;
import ru.practicum.users.dto.UserCreateDto;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.dto.UserSubscriptionDto;
import ru.practicum.users.mapper.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> get(List<Long> ids, Pageable pageable) {
        if (ids == null) {
            return userMapper.toDto(userRepository.findAll(pageable));
        } else {
            return userMapper.toDto(userRepository.findAllByIdIn(ids, pageable));
        }
    }

    @Override
    @Transactional
    public UserDto create(UserCreateDto userCreateDto) {
        var user = userRepository.save(userMapper.toModel(userCreateDto));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void delete(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public UserSubscriptionDto subscribe(long userId, long subscriptionId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var subscription = userRepository.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND
                        .getFormatMessage(subscriptionId)));
        if (!user.getSubscriptions().add(subscription)) {
            throw new AccessDeniedException(ErrorMessages.ALREADY_SUBSCRIBED.getFormatMessage(String.valueOf(userId),
                    String.valueOf(subscriptionId)));
        }
        var updatedUser = userRepository.save(user);
        return userMapper.toSubscriptionDto(updatedUser);
    }

    @Override
    @Transactional
    public UserSubscriptionDto unsubscribe(long userId, long subscriptionId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var subscription = userRepository.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND
                        .getFormatMessage(subscriptionId)));
        if (!user.getSubscriptions().remove(subscription)) {
            throw new AccessDeniedException(ErrorMessages.NOT_SUBSCRIBED.getFormatMessage(String.valueOf(userId),
                    String.valueOf(subscriptionId)));
        }
        var updatedUser = userRepository.save(user);
        return userMapper.toSubscriptionDto(updatedUser);
    }

    @Override
    public UserSubscriptionDto getSubscriptions(long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        return userMapper.toSubscriptionDto(user);
    }
}
