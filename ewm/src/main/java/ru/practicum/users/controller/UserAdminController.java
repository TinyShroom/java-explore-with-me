package ru.practicum.users.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.logging.Logging;
import ru.practicum.users.dto.UserCreateDto;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    @Logging
    @GetMapping
    public List<UserDto> get(@RequestParam(required = false) List<Long> ids,
                             @RequestParam(defaultValue = "0") @Min(0) int from,
                             @RequestParam(defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        return userService.get(ids, pageable);
    }

    @Logging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid UserCreateDto user) {
        return userService.create(user);
    }

    @Logging
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long userId) {
        userService.delete(userId);
    }
}
