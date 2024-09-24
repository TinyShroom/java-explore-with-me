package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;
import ru.practicum.users.dao.SubscriptionsStorage;
import ru.practicum.users.dao.UserRepository;
import ru.practicum.users.dto.UserCreateDto;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.mapper.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SubscriptionsStorage subscriptionsStorage;

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
}
