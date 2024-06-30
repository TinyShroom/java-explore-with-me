package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.StatsRepository;
import ru.practicum.dto.EndpointHitCreateDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.mapper.EndpointHitMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final EndpointHitMapper endpointHitMapper;

    @Override
    @Transactional
    public void create(EndpointHitCreateDto endpointHitCreateDto) {
        statsRepository.save(endpointHitMapper.toModel(endpointHitCreateDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (uris == null) {
            if (unique) {
                return statsRepository.findUnique(start, end);
            } else {
                return statsRepository.findAll(start, end);
            }
        } else {
            if (unique) {
                return statsRepository.findUniqueByUris(start, end, uris);
            } else {
                return statsRepository.findByUris(start, end, uris);
            }
        }
    }
}
