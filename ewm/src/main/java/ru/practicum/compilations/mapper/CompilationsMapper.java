package ru.practicum.compilations.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import ru.practicum.compilations.dto.CompilationCreateDto;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.CompilationUpdateDto;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.events.model.Event;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    Compilation toModel(CompilationCreateDto compilationCreateDto, List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toModel(@MappingTarget final Compilation compilation,
                 CompilationUpdateDto compilationUpdateDto, List<Event> events);

    CompilationDto toDto(Compilation compilation);

    List<CompilationDto> toDto(Page<Compilation> compilations);
}