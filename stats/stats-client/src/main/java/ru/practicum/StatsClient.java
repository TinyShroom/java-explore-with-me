package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitCreateDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.logging.Logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsClient {

    private final RestTemplate rest;
    private final String serverUrl;

    public StatsClient(@Value("${stats-server.url}") String serverUrl) {
        this.rest = new RestTemplate();
        this.serverUrl = serverUrl;
    }

    @Logging
    public List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        var endpoint = "/stats";
        var urisString = "";
        if (uris != null) {
            urisString += uris.stream()
                    .map(u -> "&uris=" + u)
                    .collect(Collectors.joining());
        }
        var path = serverUrl +
                endpoint +
                "?start=" +
                start.format(formatter) +
                "&end=" +
                end.format(formatter) +
                urisString +
                "&unique=" +
                unique;
        ResponseEntity<List<ViewStatsDto>> response = rest.exchange(path, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    @Logging
    public void post(EndpointHitCreateDto endpointHitCreateDto) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(endpointHitCreateDto);
        var endpoint = "/hit";
        var path = serverUrl + endpoint;
        rest.exchange(path, HttpMethod.POST, requestEntity, Object.class);
    }
}
