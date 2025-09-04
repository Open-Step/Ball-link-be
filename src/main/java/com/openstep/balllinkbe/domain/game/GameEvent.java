package com.openstep.balllinkbe.domain.game;

import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GameEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "game_id")
    private Game game;

    private LocalDateTime ts;
    private int period;
    private String clockTime;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    private GameLineupPlayer.Side teamSide;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "player_id")
    private Player player;

    @Enumerated(EnumType.STRING)
    private EventType type;

    @Lob
    private String meta;

    public enum EventType {
        SCORE, FOUL, TIMEOUT, SUBSTITUTION,
        ASSIST, STEAL, BLOCK, TURNOVER,
        REBOUND_O, REBOUND_D,
        PERIOD_START, PERIOD_END,
        CLOCK_UPDATE, SHOTCLOCK_RESET,
        NOTE
    }
}
