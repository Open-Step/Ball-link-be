package com.openstep.balllinkbe.domain.tournament;

import com.openstep.balllinkbe.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_teams",
        uniqueConstraints = @UniqueConstraint(name = "uk_tournament_team", columnNames = {"tournament_id","team_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TournamentTeam {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id")
    private Team team;

    private Integer seed;
    private LocalDateTime createdAt;
}
