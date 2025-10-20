package com.openstep.balllinkbe.features.score.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GamePlayerStat;
import com.openstep.balllinkbe.domain.game.GameTeamStat;
import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.features.score.repository.GamePlayerStatScoreRepository;
import com.openstep.balllinkbe.features.score.repository.GameTeamStatScoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatAggregator {

    private final EntityManager em;
    private final GamePlayerStatScoreRepository playerRepo;
    private final GameTeamStatScoreRepository teamRepo;

    /* ===========================================================
       득점 이벤트 (score.add)
       =========================================================== */
    @Transactional
    public void applyScore(Long gameId, Map<String, Object> data) {
        try {
            int pts = ((Number) data.getOrDefault("pts", 0)).intValue();
            String teamSide = (String) data.getOrDefault("team", null);
            Long playerId = data.get("playerId") != null ? ((Number) data.get("playerId")).longValue() : null;

            if (pts == 0 || teamSide == null) return;

            Game game = em.getReference(Game.class, gameId);
            Player player = playerId != null ? em.getReference(Player.class, playerId) : null;
            Team team = player != null ? player.getTeam() : getTeamBySide(game, teamSide);

            // 팀 스탯 업데이트
            GameTeamStat teamStat = getOrCreateTeamStat(game, team);
            teamStat.setPts(teamStat.getPts() + pts);
            teamRepo.save(teamStat);

            // 선수 스탯 업데이트
            if (player != null) {
                GamePlayerStat stat = getOrCreatePlayerStat(game, player);
                stat.setPts(stat.getPts() + pts);
                playerRepo.save(stat);
            }

            log.info("[Score] +{} pts | team={} player={}", pts, team.getName(), player != null ? player.getName() : "-");

        } catch (Exception e) {
            log.error("Error applying score stat: {}", e.getMessage());
        }
    }

    /* ===========================================================
       파울 이벤트 (foul.add)
       =========================================================== */
    @Transactional
    public void applyFoul(Long gameId, Map<String, Object> data) {
        try {
            String teamSide = (String) data.getOrDefault("team", null);
            Long playerId = data.get("playerId") != null ? ((Number) data.get("playerId")).longValue() : null;

            Game game = em.getReference(Game.class, gameId);
            Player player = playerId != null ? em.getReference(Player.class, playerId) : null;
            Team team = player != null ? player.getTeam() : getTeamBySide(game, teamSide);

            // 팀 파울 누적
            GameTeamStat teamStat = getOrCreateTeamStat(game, team);
            teamStat.setPf(teamStat.getPf() + 1);
            teamRepo.save(teamStat);

            // 선수 파울 누적
            if (player != null) {
                GamePlayerStat stat = getOrCreatePlayerStat(game, player);
                stat.setPf(stat.getPf() + 1);
                playerRepo.save(stat);
            }

            log.info("[Foul] team={} player={}", team.getName(), player != null ? player.getName() : "-");

        } catch (Exception e) {
            log.error("Error applying foul stat: {}", e.getMessage());
        }
    }

    /* ===========================================================
       경기 종료 (game.finish)
       =========================================================== */
    @Transactional
    public void finalizeGame(Long gameId) {
        try {
            Game game = em.getReference(Game.class, gameId);
            game.setState(Game.State.FINISHED);
            game.setFinishedAt(java.time.LocalDateTime.now());
            em.merge(game);

            log.info("Game {} marked as FINISHED", gameId);

        } catch (Exception e) {
            log.error("❌ Error finalizing game: {}", e.getMessage());
        }
    }

    /* ===========================================================
       헬퍼 메서드들
       =========================================================== */
    private GameTeamStat getOrCreateTeamStat(Game game, Team team) {
        Optional<GameTeamStat> existing = teamRepo.findByGameAndTeam(game, team);
        if (existing.isPresent()) return existing.get();

        GameTeamStat stat = GameTeamStat.builder()
                .game(game)
                .team(team)
                .pts(0).reb(0).ast(0).pf(0).stl(0).blk(0).tov(0)
                .build();
        return teamRepo.save(stat);
    }

    private GamePlayerStat getOrCreatePlayerStat(Game game, Player player) {
        Optional<GamePlayerStat> existing = playerRepo.findByGameAndPlayer(game, player);
        if (existing.isPresent()) return existing.get();

        GamePlayerStat stat = GamePlayerStat.builder()
                .game(game)
                .team(player.getTeam())
                .player(player)
                .pts(0).reb(0).ast(0).pf(0).stl(0).blk(0).tov(0)
                .build();
        return playerRepo.save(stat);
    }

    private Team getTeamBySide(Game game, String side) {
        return "HOME".equalsIgnoreCase(side) ? game.getHomeTeam() : game.getAwayTeam();
    }
}
