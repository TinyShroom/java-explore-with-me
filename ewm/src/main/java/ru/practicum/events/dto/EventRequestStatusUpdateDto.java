package ru.practicum.events.dto;

import lombok.Data;
import ru.practicum.events.constraint.RejectedOrConfirmed;
import ru.practicum.requests.model.RequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateDto {

    @RejectedOrConfirmed
    private RequestStatus status;
    private List<Long> requestIds;
}
