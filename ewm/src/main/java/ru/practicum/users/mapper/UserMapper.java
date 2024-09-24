package ru.practicum.users.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import ru.practicum.users.dto.SubscriptionDto;
import ru.practicum.users.dto.UserCreateDto;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.dto.UserShortDto;
import ru.practicum.users.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toModel(UserCreateDto userCreateDto);

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);

    List<UserDto> toDto(Page<User> users);

    SubscriptionDto toSubscriptionDto(User user);
}
