package com.openstep.balllinkbe.features.score.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameEvent;
import com.openstep.balllinkbe.domain.game.GameLineupPlayer;
import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.features.score.repository.GameEventRepository;
import com.openstep.balllinkbe.features.team_manage.repository.PlayerRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameEventWriter {

    private final GameEventRepository gameEventRepository;
    private final PlayerRepository playerRepo;
    private final EntityManager em;
    private final ObjectMapper mapper = new ObjectMapper();

    private GameEvent saveEvent(Long gameId, GameEvent.EventType type, Map<String, Object> data) {
        Game game = em.getReference(Game.class, gameId);

        // 팀 / 팀사이드
        GameLineupPlayer.Side teamSide = null;
        if (data.containsKey("team")) {
            try { teamSide = GameLineupPlayer.Side.valueOf(data.get("team").toString().toUpperCase()); }
            catch (Exception ignored) {}
        }

        Team team = null;
        if (teamSide != null)
            team = ("HOME".equalsIgnoreCase(teamSide.name()) ? game.getHomeTeam() : game.getAwayTeam());

        // 선수 해석
        Player player = null;
        if (data.containsKey("playerId")) {
            player = em.getReference(Player.class, ((Number) data.get("playerId")).longValue());
        } else if (data.containsKey("number") && team != null) {
            Short num = ((Number) data.get("number")).shortValue();
            player = playerRepo.findByTeamIdAndNumberAndIsActiveTrue(team.getId(), num).orElse(null);
        }

        String meta = "";
        try { meta = mapper.writeValueAsString(data); } catch (Exception ignored) {}

        int quarter = ((Number) data.getOrDefault("quarter", 1)).intValue();

        GameEvent ev = GameEvent.builder()
                .game(game)
                .team(team)
                .player(player)
                .period(quarter)
                .clockTime((String) data.getOrDefault("clockTime", null))
                .teamSide(teamSide)
                .meta(meta)
                .ts(LocalDateTime.now())
                .type(type)
                .build();

        return gameEventRepository.save(ev);
    }

    public GameEvent recordScore(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.SCORE, data);
    }

    public GameEvent recordRebound(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.REBOUND, data);
    }

    public GameEvent recordFoul(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.FOUL, data);
    }

    public GameEvent recordSubstitution(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.SUBSTITUTION, data);
    }

    public GameEvent recordClock(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.CLOCK_UPDATE, data);
    }

    public GameEvent recordPeriodStart(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.PERIOD_START, data);
    }

    public GameEvent recordPeriodEnd(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.PERIOD_END, data);
    }

    public GameEvent recordTimeout(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.TIMEOUT, data);
    }

    public GameEvent recordGameFinish(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.GAME_FINISH, data);
    }

    public GameEvent recordAssist(Long gameId, Map<String,Object> data) {
        return saveEvent(gameId, GameEvent.EventType.ASSIST, data);
    }

    public GameEvent recordSteal(Long gameId, Map<String,Object> data) {
        return saveEvent(gameId, GameEvent.EventType.STEAL, data);
    }

    public GameEvent recordBlock(Long gameId, Map<String,Object> data) {
        return saveEvent(gameId, GameEvent.EventType.BLOCK, data);
    }

    public Map<String, Object> toPbpEvent(GameEvent ev) {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", ev.getId());
        data.put("type", ev.getType() != null ? ev.getType().name() : null);
        data.put("team", ev.getTeamSide() != null ? ev.getTeamSide().name() : null);
        data.put("playerId", ev.getPlayer() != null ? ev.getPlayer().getId() : null);
        data.put("meta", ev.getMeta());
        return Map.of("type", "event", "action", "pbp.append", "data", data);
    }

    public GameEvent recordShotclockReset(Long gameId, Map<String, Object> data) {
        data.put("seconds", 24);
        return saveEvent(gameId, GameEvent.EventType.SHOTCLOCK_RESET, data);
    }

    public GameEvent recordShotclockReset14(Long gameId, Map<String, Object> data) {
        data.put("seconds", 14);
        return saveEvent(gameId, GameEvent.EventType.SHOTCLOCK_RESET, data);
    }

}
