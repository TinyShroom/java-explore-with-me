package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.dto.EndpointHitCreateDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    private static ObjectMapper mapper;

    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void postOk() throws Exception {
        var stringTime = "2020-10-13 10:23:45";
        var content = EndpointHitCreateDto.builder()
                .app("app")
                .uri("uri")
                .ip("192.168.0.1")
                .timestamp(LocalDateTime.parse(stringTime, FORMATTER))
                .build();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(content));
        mockMvc.perform(mockRequest)
                .andExpect(status().isCreated());
        verify(statsService, Mockito.times(1))
                .create(content);
    }

    @Test
    void postWithoutAppFail() throws Exception {
        var stringTime = "2020-10-13 10:23:45";
        var content = EndpointHitCreateDto.builder()
                .uri("uri")
                .ip("192.168.0.1")
                .timestamp(LocalDateTime.parse(stringTime, FORMATTER))
                .build();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(content));
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }

    @Test
    void postWithoutUriFail() throws Exception {
        var stringTime = "2020-10-13 10:23:45";
        var content = EndpointHitCreateDto.builder()
                .app("app")
                .ip("192.168.0.1")
                .timestamp(LocalDateTime.parse(stringTime, FORMATTER))
                .build();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(content));
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }

    @Test
    void postWithoutIpFail() throws Exception {
        var stringTime = "2020-10-13 10:23:45";
        var content = EndpointHitCreateDto.builder()
                .app("app")
                .uri("uri")
                .timestamp(LocalDateTime.parse(stringTime, FORMATTER))
                .build();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(content));
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }

    @Test
    void postWithoutTimestampFail() throws Exception {
        var content = EndpointHitCreateDto.builder()
                .app("app")
                .uri("uri")
                .ip("192.168.0.1")
                .build();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(content));
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }

    @Test
    void postIpFail() throws Exception {
        var stringTime = "2020-10-13 10:23:45";
        var content = EndpointHitCreateDto.builder()
                .app("app")
                .uri("uri")
                .ip("192.168.0.")
                .timestamp(LocalDateTime.parse(stringTime, FORMATTER))
                .build();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(content));
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
        content.setIp("192.256.0.1");
        mockRequest = MockMvcRequestBuilders.post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(content));
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }

    @Test
    void getOk() throws Exception {

        var start = "2020-10-13 10:23:45";
        var end = "2020-10-14 10:23:46";
        var uris = List.of("/uri1", "/uri2");
        var unique = true;
        var answer = List.of(
                ViewStatsDto.builder()
                        .uri(uris.get(0))
                        .app("app")
                        .hits(5)
                        .build(),
                ViewStatsDto.builder()
                        .uri(uris.get(1))
                        .app("app")
                        .hits(4)
                        .build()

        );
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.get("/stats")
                .param("start", start)
                .param("end", end);
        when(statsService.get(LocalDateTime.parse(start, FORMATTER), LocalDateTime.parse(end, FORMATTER),
                null, false))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockRequest = MockMvcRequestBuilders.get("/stats")
                .param("start", start)
                .param("end", end)
                .param("uris", uris.get(0))
                .param("uris", uris.get(1))
                .param("unique", String.valueOf(unique));
        when(statsService.get(LocalDateTime.parse(start, FORMATTER), LocalDateTime.parse(end, FORMATTER),
                uris, unique))
                .thenReturn(answer);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(answer.size())));
    }

    @Test
    void getEndBeforeStartFail() throws Exception {

        var end = "2020-10-13 10:23:45";
        var start = "2020-10-14 10:23:46";
        var uris = List.of("/uri1", "/uri2");
        var unique = true;

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.get("/stats")
                .param("start", start)
                .param("end", end)
                .param("uris", uris.get(0))
                .param("uris", uris.get(1))
                .param("unique", String.valueOf(unique));

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.END_BEFORE_START.getMessage())));
    }

    @Test
    void getWithoutStartFail() throws Exception {

        var end = "2020-10-13 10:23:45";
        var uris = List.of("/uri1", "/uri2");
        var unique = true;

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.get("/stats")
                .param("end", end)
                .param("uris", uris.get(0))
                .param("uris", uris.get(1))
                .param("unique", String.valueOf(unique));

        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getWithoutEndFail() throws Exception {

        var start = "2020-10-13 10:23:45";
        var uris = List.of("/uri1", "/uri2");
        var unique = true;

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.get("/stats")
                .param("start", start)
                .param("uris", uris.get(0))
                .param("uris", uris.get(1))
                .param("unique", String.valueOf(unique));

        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }
}