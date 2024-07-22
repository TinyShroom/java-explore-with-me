package ru.practicum.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilations.dao.CompilationsRepository;
import ru.practicum.compilations.dto.CompilationCreateDto;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.CompilationUpdateDto;
import ru.practicum.compilations.mapper.CompilationsMapper;
import ru.practicum.events.dao.EventRepository;
import ru.practicum.events.model.Event;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationsServiceImpl implements CompilationsService {

    private final CompilationsRepository compilationsRepository;
    private final EventRepository eventRepository;
    private final CompilationsMapper compilationsMapper;

    @Override
    @Transactional
    public CompilationDto create(CompilationCreateDto compilationCreateDto) {
        List<Event> events = findEvents(compilationCreateDto.getEvents());
        if (events == null) events = Collections.emptyList();
        var result = compilationsRepository.save(compilationsMapper.toModel(compilationCreateDto, events));
        return compilationsMapper.toDto(result);
    }

    @Override
    @Transactional
    public void delete(long compId) {
        compilationsRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto update(long compId, CompilationUpdateDto compilationUpdateDto) {
        var oldCompilation = compilationsRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.CATEGORY_NOT_FOUND.getFormatMessage(compId)));
        List<Event> events = findEvents(compilationUpdateDto.getEvents());
        compilationsMapper.toModel(oldCompilation, compilationUpdateDto, events);
        return compilationsMapper.toDto(compilationsRepository.save(oldCompilation));
    }

    private List<Event> findEvents(Set<Long> eventIds) {
        if (eventIds == null) return null;
        if (eventIds.isEmpty()) return Collections.emptyList();
        var events = eventRepository.findAllById(eventIds);
        if (events.size() != eventIds.size()) {
            eventIds.removeAll(events.stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet())
            );
            throw new NotFoundException(ErrorMessages.EVENTS_NOT_FOUND.getFormatMessage(
                    eventIds.toString()));
        }
        return events;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageable) {
        if (pinned == null) {
            return compilationsMapper.toDto(compilationsRepository.findAll(pageable));
        } else {
            return compilationsMapper.toDto(compilationsRepository.findAllByPinned(pinned, pageable));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto get(long compId) {
        var compilation = compilationsRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessages.COMPILATION_NOT_FOUND.getFormatMessage(compId)));
        return compilationsMapper.toDto(compilation);
    }
}