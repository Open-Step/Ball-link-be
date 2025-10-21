package com.openstep.balllinkbe.features.user.repository;

import com.openstep.balllinkbe.features.user.repository.projection.PlayerCareerRecentProjection;
import com.openstep.balllinkbe.features.user.repository.projection.PlayerCareerSeasonProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.openstep.balllinkbe.domain.game.GamePlayerStat;

import java.util.List;

@Repository
public interface MyCareerRepository extends JpaRepository<GamePlayerStat, Long> {

    /** 최근 경기 (최대 5경기) */
    @Query(value = """
        SELECT 
            g.id AS gameId,
            COALESCE(g.started_at, g.scheduled_at) AS date,
            CASE 
                WHEN g.home_team_id = s.team_id THEN at.name 
                ELSE ht.name 
            END AS opponent,
            s.pts AS pts, s.reb AS reb, s.ast AS ast, s.stl AS stl, s.blk AS blk
        FROM game_player_stats s
        JOIN games g ON g.id = s.game_id
        LEFT JOIN teams ht ON ht.id = g.home_team_id
        LEFT JOIN teams at ON at.id = g.away_team_id
        WHERE s.user_id = :userId
        ORDER BY g.started_at DESC
        LIMIT 5
    """, nativeQuery = true)
    List<PlayerCareerRecentProjection> findRecentGames(@Param("userId") Long userId);


    /** 연도별 통산 기록 */
    @Query(value = """
        SELECT 
            t.season AS season,
            COUNT(DISTINCT g.id) AS games,
            SUM(s.pts) AS pts,
            SUM(s.reb) AS reb,
            SUM(s.ast) AS ast,
            SUM(s.stl) AS stl,
            SUM(s.blk) AS blk,
            SUM(s.fg2) AS fg2,
            SUM(s.fg3) AS fg3,
            SUM(s.ft)  AS ft
        FROM game_player_stats s
        JOIN games g ON g.id = s.game_id
        JOIN tournaments t ON t.id = g.tournament_id
        WHERE s.user_id = :userId
        GROUP BY t.season
        ORDER BY t.season DESC
    """, nativeQuery = true)
    List<PlayerCareerSeasonProjection> findSeasonStats(@Param("userId") Long userId);
}
