package ru.practicum.users.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.users.dto.UserCreateDto;
import ru.practicum.users.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> get(List<Long> ids, Pageable pageable);

    UserDto create(UserCreateDto user);

    void delete(long usrId);
}
