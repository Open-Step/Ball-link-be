package com.openstep.balllinkbe.features.team_record.repository;

import com.openstep.balllinkbe.domain.game.GameTeamStat;
import com.openstep.balllinkbe.features.team_record.repository.projection.GameHeaderProjection;
import com.openstep.balllinkbe.features.team_record.repository.projection.PlayerAggregateProjection;
import com.openstep.balllinkbe.features.team_record.repository.projection.PlayerGameProjection;
import com.openstep.balllinkbe.features.team_record.repository.projection.PlayerLineProjection;
import com.openstep.balllinkbe.features.team_record.repository.projection.TournamentAggProjection;
import com.openstep.balllinkbe.features.team_record.repository.projection.TournamentGameProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRecordRepository extends JpaRepository<GameTeamStat, Long> {

    /* ───────────────────────────────
     *  1) 팀 대회참가기록 / 2) 팀 통산기록
     * ─────────────────────────────── */

    @Query("""
        SELECT s 
        FROM GameTeamStat s 
        JOIN FETCH s.game g 
        JOIN FETCH g.tournament t 
        WHERE t.id = :tournamentId 
          AND s.team.id = :teamId
    """)
    List<GameTeamStat> findByTournamentIdAndTeamId(@Param("tournamentId") Long tournamentId,
                                                   @Param("teamId") Long teamId);

    @Query("""
        SELECT s 
        FROM GameTeamStat s 
        JOIN FETCH s.game g 
        LEFT JOIN FETCH g.tournament t 
        WHERE s.team.id = :teamId 
          AND (:season = 'ALL' OR t.season = :season)
    """)
    List<GameTeamStat> findByTeamIdAndSeason(@Param("teamId") Long teamId,
                                             @Param("season") String season);

    /* 승/패 카운트 (대회/시즌) — 상대팀과 점수 비교 */
    @Query(value = """
        SELECT SUM(CASE WHEN s.pts > opp.pts THEN 1 ELSE 0 END) 
        FROM game_team_stats s
        JOIN game_team_stats opp ON opp.game_id = s.game_id AND opp.team_id <> s.team_id
        JOIN games g ON g.id = s.game_id
        WHERE g.tournament_id = :tournamentId
          AND s.team_id = :teamId
    """, nativeQuery = true)
    Integer countWinsInTournament(@Param("tournamentId") Long tournamentId,
                                  @Param("teamId") Long teamId);

    @Query(value = """
        SELECT SUM(CASE WHEN s.pts < opp.pts THEN 1 ELSE 0 END) 
        FROM game_team_stats s
        JOIN game_team_stats opp ON opp.game_id = s.game_id AND opp.team_id <> s.team_id
        JOIN games g ON g.id = s.game_id
        WHERE g.tournament_id = :tournamentId
          AND s.team_id = :teamId
    """, nativeQuery = true)
    Integer countLossesInTournament(@Param("tournamentId") Long tournamentId,
                                    @Param("teamId") Long teamId);

    @Query(value = """
        SELECT SUM(CASE WHEN s.pts > opp.pts THEN 1 ELSE 0 END)
        FROM game_team_stats s
        JOIN game_team_stats opp ON opp.game_id = s.game_id AND opp.team_id <> s.team_id
        JOIN games g ON g.id = s.game_id
        LEFT JOIN tournaments t ON t.id = g.tournament_id
        WHERE s.team_id = :teamId
          AND (:season = 'ALL' OR t.season = :season)
    """, nativeQuery = true)
    Integer countWinsInSeason(@Param("teamId") Long teamId, @Param("season") String season);

    @Query(value = """
        SELECT SUM(CASE WHEN s.pts < opp.pts THEN 1 ELSE 0 END)
        FROM game_team_stats s
        JOIN game_team_stats opp ON opp.game_id = s.game_id AND opp.team_id <> s.team_id
        JOIN games g ON g.id = s.game_id
        LEFT JOIN tournaments t ON t.id = g.tournament_id
        WHERE s.team_id = :teamId
          AND (:season = 'ALL' OR t.season = :season)
    """, nativeQuery = true)
    Integer countLossesInSeason(@Param("teamId") Long teamId, @Param("season") String season);


    /* ───────────────────────────────
     *  3) 선수 통산 집계 (랭킹)
     * ─────────────────────────────── */
    @Query("""
        SELECT 
            s.player.id          AS playerId,
            s.player.name        AS playerName,
            COALESCE(s.player.number, 0) AS backNumber,
            COUNT(s.id)          AS games,
            SUM(s.pts)           AS pts,
            SUM(s.reb)           AS reb,
            SUM(s.ast)           AS ast,
            SUM(s.stl)           AS stl,
            SUM(s.blk)           AS blk,
            SUM(s.fg2Made)       AS fg2Made,
            SUM(s.fg2Att)        AS fg2Att,
            SUM(s.fg3Made)       AS fg3Made,
            SUM(s.fg3Att)        AS fg3Att,
            SUM(s.ftMade)        AS ftMade,
            SUM(s.ftAtt)         AS ftAtt
        FROM GamePlayerStat s
        JOIN s.game g
        LEFT JOIN g.tournament t
        WHERE s.team.id = :teamId
          AND (:season = 'ALL' OR t.season = :season)
        GROUP BY s.player.id, s.player.name, s.player.number
    """)
    List<PlayerAggregateProjection> aggregatePlayerStats(@Param("teamId") Long teamId,
                                                         @Param("season") String season);


    /* ───────────────────────────────
     *  4) 선수 경기별 기록 (페이징)
     * ─────────────────────────────── */
    @Query("""
        SELECT 
            g.id AS gameId,
            COALESCE(g.startedAt, g.scheduledAt) AS date,
            CASE WHEN g.homeTeam.id = :teamId THEN g.awayTeam.name 
                 WHEN g.awayTeam.id = :teamId THEN g.homeTeam.name
                 ELSE g.opponentName END AS opponent,
            s.pts AS pts, s.reb AS reb, s.ast AS ast, s.stl AS stl, s.blk AS blk,
            s.fg2Made AS fg2Made, s.fg2Att AS fg2Att, s.fg3Made AS fg3Made, s.fg3Att AS fg3Att,
            s.ftMade AS ftMade, s.ftAtt AS ftAtt
        FROM GamePlayerStat s
        JOIN s.game g
        LEFT JOIN g.tournament t
        WHERE s.team.id = :teamId AND s.player.id = :playerId
          AND (:season = 'ALL' OR t.season = :season)
    """)
    Page<PlayerGameProjection> findPlayerGames(@Param("teamId") Long teamId,
                                               @Param("playerId") Long playerId,
                                               @Param("season") String season,
                                               Pageable pageable);


    /* ───────────────────────────────
     *  5) 팀 대회목록 요약
     * ─────────────────────────────── */
    @Query(value = """
        SELECT 
            t.id                AS tournamentId,
            t.name              AS tournamentName,
            t.season            AS season,
            t.status            AS status,
            t.start_date        AS startDate,
            t.end_date          AS endDate,
            COUNT(DISTINCT g.id)                                            AS games,
            SUM(CASE WHEN s.pts > opp.pts THEN 1 ELSE 0 END)               AS wins,
            SUM(CASE WHEN s.pts < opp.pts THEN 1 ELSE 0 END)               AS losses,
            SUM(s.pts)                                                     AS pts
        FROM game_team_stats s
        JOIN games g ON g.id = s.game_id
        JOIN tournaments t ON t.id = g.tournament_id
        JOIN game_team_stats opp ON opp.game_id = g.id AND opp.team_id <> s.team_id
        WHERE s.team_id = :teamId
          AND (:season = 'ALL' OR t.season = :season)
        GROUP BY t.id, t.name, t.season, t.status, t.start_date, t.end_date
        ORDER BY t.start_date DESC
    """, nativeQuery = true)
    List<TournamentAggProjection> findTournamentSummaries(@Param("teamId") Long teamId,
                                                          @Param("season") String season);


    /* ───────────────────────────────
     *  6) 대회 내 경기목록 (팀 관점, Page)
     * ─────────────────────────────── */
    @Query(value = """
        SELECT 
            g.id AS gameId,
            COALESCE(g.started_at, g.scheduled_at) AS date,
            v.name AS venueName,
            CASE 
              WHEN g.home_team_id = :teamId THEN COALESCE(at.name, g.opponent_name)
              WHEN g.away_team_id = :teamId THEN COALESCE(ht.name, g.opponent_name)
              ELSE COALESCE(ht.name, at.name)
            END AS opponentName,
            g.state AS state,
            s.pts AS myScore,
            opp.pts AS oppScore,
            CASE WHEN s.pts > opp.pts THEN 'WIN'
                 WHEN s.pts < opp.pts THEN 'LOSE'
                 ELSE 'DRAW' END AS result
        FROM games g
        JOIN game_team_stats s   ON s.game_id = g.id AND s.team_id = :teamId
        JOIN game_team_stats opp ON opp.game_id = g.id AND opp.team_id <> :teamId
        LEFT JOIN venues v ON v.id = g.venue_id
        LEFT JOIN teams ht ON ht.id = g.home_team_id
        LEFT JOIN teams at ON at.id = g.away_team_id
        WHERE g.tournament_id = :tournamentId
          AND (:status = 'ALL' OR g.state = :status)
        """,
            countQuery = """
        SELECT COUNT(1)
        FROM games g
        JOIN game_team_stats s ON s.game_id = g.id AND s.team_id = :teamId
        WHERE g.tournament_id = :tournamentId
          AND (:status = 'ALL' OR g.state = :status)
        """,
            nativeQuery = true)
    Page<TournamentGameProjection> findTournamentGames(@Param("teamId") Long teamId,
                                                       @Param("tournamentId") Long tournamentId,
                                                       @Param("status") String status,
                                                       Pageable pageable);


    /* ───────────────────────────────
     *  7) 경기 박스스코어 (헤더/팀합계/선수라인)
     * ─────────────────────────────── */
    @Query(value = """
        SELECT 
            g.id AS gameId,
            t.id AS tournamentId,
            t.name AS tournamentName,
            COALESCE(g.started_at, g.scheduled_at) AS date,
            v.name AS venueName,
            g.home_team_id AS homeTeamId,
            ht.name AS homeTeamName,
            g.away_team_id AS awayTeamId,
            at.name AS awayTeamName
        FROM games g
        LEFT JOIN tournaments t ON t.id = g.tournament_id
        LEFT JOIN teams ht ON ht.id = g.home_team_id
        LEFT JOIN teams at ON at.id = g.away_team_id
        LEFT JOIN venues v ON v.id = g.venue_id
        WHERE g.id = :gameId
    """, nativeQuery = true)
    Optional<GameHeaderProjection> getGameHeader(@Param("gameId") Long gameId);

    @Query("""
        SELECT s FROM GameTeamStat s
        JOIN FETCH s.team
        JOIN FETCH s.game g
        WHERE g.id = :gameId
    """)
    List<GameTeamStat> findTeamStatsByGame(@Param("gameId") Long gameId);

    @Query("""
        SELECT 
            s.team.id   AS teamId,
            s.player.id AS playerId,
            s.player.name AS playerName,
            s.player.number AS backNumber,
            s.player.position AS position,
            s.pts AS pts, s.reb AS reb, s.ast AS ast, s.stl AS stl, s.blk AS blk,
            s.fg2Made AS fg2Made, s.fg2Att AS fg2Att, s.fg3Made AS fg3Made, s.fg3Att AS fg3Att,
            s.ftMade AS ftMade, s.ftAtt AS ftAtt,
            s.pf AS pf, s.tov AS tov,
            s.minutes AS minutes
        FROM GamePlayerStat s
        WHERE s.game.id = :gameId
        ORDER BY s.team.id, COALESCE(s.player.number, 999), s.player.name
    """)
    List<PlayerLineProjection> findPlayerLines(@Param("gameId") Long gameId);
}
