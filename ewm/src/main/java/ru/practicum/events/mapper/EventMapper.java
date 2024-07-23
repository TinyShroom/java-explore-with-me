package ru.practicum.events.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import ru.practicum.categories.model.Category;
import ru.practicum.events.dto.EventAdminUpdateDto;
import ru.practicum.events.dto.EventCreateDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.EventUserUpdateDto;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventState;
import ru.practicum.users.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "requestModeration", source = "eventCreateDto.requestModeration", defaultValue = "true")
    Event toModel(EventCreateDto eventCreateDto, User initiator, Category category, LocalDateTime createdOn,
                  EventState state);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toModel(@MappingTarget final Event event, EventUserUpdateDto eventUpdateDto, Category category);

    EventFullDto toFullDto(Event event);

    EventFullDto toFullDto(Event event, long views, long confirmedRequests);

    List<EventShortDto> toShortDto(Iterable<Event> events);

    EventShortDto toShortDto(Event event, long views, long confirmedRequests);

    List<EventFullDto> toFullDto(Page<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget final Event event, EventAdminUpdateDto eventUpdateDto, Category category);
}