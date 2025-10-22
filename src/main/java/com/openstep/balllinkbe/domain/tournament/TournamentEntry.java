package com.openstep.balllinkbe.domain.tournament;

import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.enums.Position;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_entries",
        uniqueConstraints = @UniqueConstraint(name = "uk_entry_unique", columnNames = {"tournament_id","team_id","player_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TournamentEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "player_id")
    private Player player;

    private Short number;

    @Enumerated(EnumType.STRING)
    private Position position;

    private String note;
    private boolean locked;
}
