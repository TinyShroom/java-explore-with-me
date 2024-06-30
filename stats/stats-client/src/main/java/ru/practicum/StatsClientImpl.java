package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitCreateDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StatsClientImpl implements StatsClient {

    private final RestTemplate rest;
    private final String serverUrl;

    public StatsClientImpl(@Value("${stats-server.url}") String serverUrl) {
        this.rest = new RestTemplate();
        this.serverUrl = serverUrl;
    }

    @Override
    public List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        var uriBuilder = UriComponentsBuilder
                .fromUriString(serverUrl)
                .path("/stats")
                .queryParam("start", start.format(formatter))
                .queryParam("end", end.format(formatter))
                .queryParam("unique", unique);
        if (uris != null && !uris.isEmpty()) {
            uriBuilder.queryParam("uris", uris);
        }

        var uri = uriBuilder.build().toUri();
        try {
            ResponseEntity<List<ViewStatsDto>> response = rest.exchange(uri, HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {
                    });
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.info("StatsClientImpl.get() uri: {} http-status: {}", uri, response.getStatusCode());
            }
            return response.getBody();
        } catch (Exception e) {
            log.info("StatsClientImpl.get() uri: {} error: {}", uri, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void post(EndpointHitCreateDto endpointHitCreateDto) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(endpointHitCreateDto);
        var uri = UriComponentsBuilder
                .fromUriString(serverUrl)
                .path("/hit")
                .build().toUri();
        try {
            var result = rest.exchange(uri, HttpMethod.POST, requestEntity, Object.class);
            if (!result.getStatusCode().is2xxSuccessful()) {
                log.info("StatsClientImpl.post() uri: {} http-status: {}", uri, result.getStatusCode());
            }
        } catch (Exception e) {
            log.info("StatsClientImpl.post() uri: {} error: {}", uri, e.getMessage());
        }
    }
}
