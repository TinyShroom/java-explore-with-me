package ru.practicum;

import ru.practicum.dto.EndpointHitCreateDto;

import java.time.LocalDateTime;
import java.util.List;

public class EwmApp {

	public static void main(String[] args) {
		StatsClient statsClient = new StatsClientImpl("http://localhost:9090");
		statsClient.post(EndpointHitCreateDto.builder()
				.app("app")
				.uri("uri")
				.ip("192.168.0.1")
				.timestamp(LocalDateTime.now())
				.build()
		);
		System.out.println(statsClient.get(LocalDateTime.of(2000, 1,1, 0,0,0),
				LocalDateTime.of(3000, 1,1, 0,0,0), null, null));
		System.out.println(statsClient.get(LocalDateTime.of(2000, 1,1, 0,0,0),
				LocalDateTime.of(3000, 1,1, 0,0,0), List.of("uri"), true));
    }
}
