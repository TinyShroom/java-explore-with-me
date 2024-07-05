package ru.practicum.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilations.dao.CompilationsRepository;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.mapper.CompilationsMapper;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicCompilationsServiceImpl implements PublicCompilationsService {

    private final CompilationsRepository compilationsRepository;
    private final CompilationsMapper compilationsMapper;

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