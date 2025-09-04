package com.openstep.balllinkbe.domain.tournament;

import com.openstep.balllinkbe.domain.venue.Venue;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tournaments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tournament {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    private String season;
    private String location;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "venue_id")
    private Venue venue;

    private LocalDateTime createdAt;

    public enum Status { SCHEDULED, ONGOING, FINISHED, CANCELED }
}
