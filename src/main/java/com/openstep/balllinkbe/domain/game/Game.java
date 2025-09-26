package com.openstep.balllinkbe.domain.game;

import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.tournament.Tournament;
import com.openstep.balllinkbe.domain.venue.Venue;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Game {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    private String opponentName;

    @Enumerated(EnumType.STRING)
    private RoundCode roundCode;

    private Integer bracketOrder;

    @Enumerated(EnumType.STRING)
    private State state;

    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    private String canceledReason;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "venue_id")
    private Venue venue;

    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_scrimmage", nullable = false)
    private boolean isScrimmage = false;

    public enum RoundCode { GROUP, ROUND_OF_16, QF, SF, FINAL }
    public enum State { SCHEDULED, ONGOING, FINISHED, CANCELED }
}
