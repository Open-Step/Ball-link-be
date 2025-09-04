package com.openstep.balllinkbe.domain.game;

import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "game_lineup_players")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GameLineupPlayer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    private Side teamSide;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "player_id")
    private Player player;

    private String guestName;
    private Short guestNumber;
    private boolean isStarter;
    private Short number;

    @Enumerated(EnumType.STRING)
    private Player.Position position;

    public enum Side { HOME, AWAY }
}
