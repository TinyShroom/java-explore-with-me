package ru.practicum.events.model;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.categories.model.Category;
import ru.practicum.users.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "events")
@NamedEntityGraph(name = "events-short", attributeNodes = {
        @NamedAttributeNode("initiator"),
        @NamedAttributeNode("category")
})
@NamedEntityGraph(name = "events-full", attributeNodes = {
        @NamedAttributeNode("initiator"),
        @NamedAttributeNode("category"),
        @NamedAttributeNode("location")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String annotation;
    @JoinColumn(name = "category_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    private String description;
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User initiator;
    @JoinColumn(name = "location_id")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Location location;
    private Boolean paid;
    @Column(name = "participant_limit")
    private Integer participantLimit;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    private EventState state;
    private String title;
}