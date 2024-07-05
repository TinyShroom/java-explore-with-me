package ru.practicum.requests.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.events.model.RequestsCount;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @EntityGraph("requests")
    List<ParticipationRequest> findAllByRequesterId(long userId);

    long countByEventIdAndStatus(long eventId, RequestStatus requestStatus);

    Optional<ParticipationRequest> findByIdAndRequesterId(long requestId, long userId);

    List<ParticipationRequest> findAllByEventId(long eventId);

    List<ParticipationRequest> findAllByEventIdAndIdIn(long eventId, List<Long> requestIds);

    @Query("select new ru.practicum.events.model.RequestsCount(pr.event.id, count(pr.id)) " +
            "from ParticipationRequest as pr " +
            "where pr.status = ?2 and pr.event.id in ?1 " +
            "group by pr.event.id")
    List<RequestsCount> countAllByEventIdInAndStatus(List<Long> eventIds, RequestStatus requestStatus);
}
