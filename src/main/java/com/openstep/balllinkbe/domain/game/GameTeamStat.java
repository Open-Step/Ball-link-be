package com.openstep.balllinkbe.domain.game;

import com.openstep.balllinkbe.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "game_team_stats",
        uniqueConstraints = @UniqueConstraint(name = "uk_game_team", columnNames = {"game_id", "team_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameTeamStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private int pts;
    private int reb;
    private int ast;
    private int stl;
    private int blk;
    private int tov;
    private int pf;

    /** 추가: 팀 단위 슛 기록 */
    @Column(columnDefinition = "INT DEFAULT 0")
    private int fg2;  // 2점슛 성공 수

    @Column(columnDefinition = "INT DEFAULT 0")
    private int fg3;  // 3점슛 성공 수

    @Column(columnDefinition = "INT DEFAULT 0")
    private int ft;   // 자유투 성공 수
}
