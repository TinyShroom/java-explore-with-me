package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("select new ru.practicum.model.ViewStats(h.app, h.uri, count(h.ip) as hits) " +
            "from EndpointHit h " +
            "where h.timestamp between ?1 and ?2 " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<ViewStats> findAll(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.model.ViewStats(h.app, h.uri, count(distinct(h.ip)) as hits) " +
            "from EndpointHit h " +
            "where h.timestamp between ?1 and ?2 " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<ViewStats> findUnique(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.model.ViewStats(h.app, h.uri, count(h.ip) as hits) " +
            "from EndpointHit h " +
            "where h.uri in ?3 and h.timestamp between ?1 and ?2 " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<ViewStats> findByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.model.ViewStats(h.app, h.uri, count(distinct(h.ip)) as hits) " +
            "from EndpointHit h " +
            "where h.uri in ?3 and h.timestamp between ?1 and ?2 " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<ViewStats> findUniqueByUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}
