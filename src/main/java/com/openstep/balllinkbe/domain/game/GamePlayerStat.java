package com.openstep.balllinkbe.domain.game;

import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "game_player_stats",
        uniqueConstraints = @UniqueConstraint(name = "uk_game_player", columnNames = {"game_id","player_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GamePlayerStat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "player_id")
    private Player player;

    private int pts;
    private int reb;
    private int ast;
    private int stl;
    private int blk;

    private int fg2Made;
    private int fg2Att;
    private int fg3Made;
    private int fg3Att;
    private int ftMade;
    private int ftAtt;

    private int tov;
    private int pf;

    private BigDecimal minutes;
}
