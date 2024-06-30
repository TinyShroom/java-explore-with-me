package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.dto.EndpointHitCreateDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class StatsServiceImplTest {

    private final StatsService statsService;
    private final EndpointHitMapper endpointHitMapper;
    private final EntityManager em;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createOk() {
        var stringTime = "2020-10-13 10:23:45";
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        var endpointHitCreateDto = EndpointHitCreateDto.builder()
                .app("app")
                .uri("uri")
                .ip("192.168.0.1")
                .timestamp(LocalDateTime.parse(stringTime, formatter))
                .build();
        statsService.create(endpointHitCreateDto);

        var result = em.createQuery("select h from EndpointHit h", EndpointHit.class)
                .getResultList()
                .stream()
                .max(Comparator.comparingLong(EndpointHit::getId)).get();
        assertThat(result.getApp()).isEqualTo(endpointHitCreateDto.getApp());
        assertThat(result.getIp()).isEqualTo(endpointHitCreateDto.getIp());
        assertThat(result.getUri()).isEqualTo(endpointHitCreateDto.getUri());
        assertThat(result.getTimestamp()).isEqualTo(endpointHitCreateDto.getTimestamp());
    }

    @Test
    public void getAllOk() {
        List<String> uris = null;
        Boolean unique = false;
        var allEndpointHit = em.createQuery("select h from EndpointHit h", EndpointHit.class)
                .getResultList();
        var start = allEndpointHit.stream()
                .min(Comparator.comparing(EndpointHit::getTimestamp))
                .get().getTimestamp().minusSeconds(1);
        var end = allEndpointHit.stream()
                .max(Comparator.comparing(EndpointHit::getTimestamp))
                .get().getTimestamp().plusSeconds(1);
        var expected = allEndpointHit.stream()
//                .filter(h -> start.isBefore(h.getTimestamp()))
//                .filter(h -> end.isAfter(h.getTimestamp()))
//                .filter(h -> start.isBefore(h.getTimestamp()))
                .collect(Collectors.groupingBy(EndpointHit::getApp,
                        Collectors.groupingBy(EndpointHit::getUri, Collectors.toList())));
        List<ViewStatsDto> viewStats = new ArrayList<>();
        for (var app : expected.keySet()) {
            var urisMap = expected.get(app);
            for (var uri : urisMap.keySet()) {
                viewStats.add(new ViewStatsDto(app, uri, urisMap.get(uri).size()));
            }
        }
        viewStats.sort(Comparator.comparingLong(ViewStatsDto::getHits).reversed());
        var result = statsService.get(start, end, uris, unique);
        assertThat(result).hasSize(viewStats.size());
        assertThat(result).usingRecursiveComparison().isEqualTo(viewStats);
    }

    @Test
    public void getOk() {
        List<String> uris = null;
        Boolean unique = false;
        var allEndpointHit = em.createQuery("select h from EndpointHit h", EndpointHit.class)
                .getResultList();
        var start = allEndpointHit.stream()
                .min(Comparator.comparing(EndpointHit::getTimestamp))
                .get().getTimestamp().plusSeconds(1);
        var end = allEndpointHit.stream()
                .max(Comparator.comparing(EndpointHit::getTimestamp))
                .get().getTimestamp().minusSeconds(1);
        var expected = allEndpointHit.stream()
                .filter(h -> start.isBefore(h.getTimestamp()))
                .filter(h -> end.isAfter(h.getTimestamp()))
                .collect(Collectors.groupingBy(EndpointHit::getApp,
                        Collectors.groupingBy(EndpointHit::getUri, Collectors.toList())));
        List<ViewStatsDto> viewStats = new ArrayList<>();
        for (var app : expected.keySet()) {
            var urisMap = expected.get(app);
            for (var uri : urisMap.keySet()) {
                viewStats.add(new ViewStatsDto(app, uri, urisMap.get(uri).size()));
            }
        }
        viewStats.sort(Comparator.comparingLong(ViewStatsDto::getHits).reversed()
                .thenComparing(ViewStatsDto::getApp)
                .thenComparing(ViewStatsDto::getUri));
        var result = statsService.get(start, end, uris, unique);
        assertThat(result).hasSize(viewStats.size());
        assertThat(result).usingRecursiveComparison().isEqualTo(viewStats);
    }

    @Test
    public void getUniqueOk() {
        List<String> uris = null;
        Boolean unique = true;
        var allEndpointHit = em.createQuery("select h from EndpointHit h", EndpointHit.class)
                .getResultList();
        var start = allEndpointHit.stream()
                .min(Comparator.comparing(EndpointHit::getTimestamp))
                .get().getTimestamp().plusSeconds(1);
        var end = allEndpointHit.stream()
                .max(Comparator.comparing(EndpointHit::getTimestamp))
                .get().getTimestamp().minusSeconds(1);
        var expected = allEndpointHit.stream()
                .filter(h -> start.isBefore(h.getTimestamp()))
                .filter(h -> end.isAfter(h.getTimestamp()))
                .collect(Collectors.groupingBy(EndpointHit::getApp,
                        Collectors.groupingBy(EndpointHit::getUri, Collectors.toList())));
        List<ViewStatsDto> viewStats = new ArrayList<>();
        for (var app : expected.keySet()) {
            var urisMap = expected.get(app);
            for (var uri : urisMap.keySet()) {
                viewStats.add(new ViewStatsDto(app, uri, urisMap.get(uri).stream()
                        .map(EndpointHit::getIp)
                        .collect(Collectors.toSet()).size()));
            }
        }
        viewStats.sort(Comparator.comparingLong(ViewStatsDto::getHits).reversed()
                .thenComparing(ViewStatsDto::getApp)
                .thenComparing(ViewStatsDto::getUri));
        var result = statsService.get(start, end, uris, unique);
        assertThat(result).hasSize(viewStats.size());
        assertThat(result).usingRecursiveComparison().isEqualTo(viewStats);
    }

    @Test
    public void getUniqueUriInOk() {
        List<String> uris = List.of("/first");
        Set<String> urisSet = new HashSet<>(uris);
        Boolean unique = true;
        var allEndpointHit = em.createQuery("select h from EndpointHit h", EndpointHit.class)
                .getResultList();
        var start = allEndpointHit.stream()
                .min(Comparator.comparing(EndpointHit::getTimestamp))
                .get().getTimestamp().plusSeconds(1);
        var end = allEndpointHit.stream()
                .max(Comparator.comparing(EndpointHit::getTimestamp))
                .get().getTimestamp().minusSeconds(1);
        var expected = allEndpointHit.stream()
                .filter(h -> start.isBefore(h.getTimestamp()))
                .filter(h -> end.isAfter(h.getTimestamp()))
                .filter(h -> urisSet.contains(h.getUri()))
                .collect(Collectors.groupingBy(EndpointHit::getApp,
                        Collectors.groupingBy(EndpointHit::getUri, Collectors.toList())));
        List<ViewStatsDto> viewStats = new ArrayList<>();
        for (var app : expected.keySet()) {
            var urisMap = expected.get(app);
            for (var uri : urisMap.keySet()) {
                viewStats.add(new ViewStatsDto(app, uri, urisMap.get(uri).stream()
                        .map(EndpointHit::getIp)
                        .collect(Collectors.toSet()).size()));
            }
        }
        viewStats.sort(Comparator.comparingLong(ViewStatsDto::getHits).reversed()
                .thenComparing(ViewStatsDto::getApp)
                .thenComparing(ViewStatsDto::getUri));
        var result = statsService.get(start, end, uris, unique);
        assertThat(result).hasSize(viewStats.size());
        assertThat(result).usingRecursiveComparison().isEqualTo(viewStats);
    }
}