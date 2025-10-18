package com.openstep.balllinkbe.features.score.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameEvent;
import com.openstep.balllinkbe.domain.game.GameLineupPlayer;
import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.features.score.repository.GameEventRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameEventWriter {

    private final GameEventRepository gameEventRepository;
    private final EntityManager em;
    private final ObjectMapper mapper = new ObjectMapper();

    /** 공통 Insert */
    private GameEvent saveEvent(Long gameId, GameEvent.EventType type, Map<String, Object> data) {
        Game game = em.getReference(Game.class, gameId);

        Team team = null;
        if (data.containsKey("teamId")) {
            team = em.getReference(Team.class, ((Number) data.get("teamId")).longValue());
        }

        Player player = null;
        if (data.containsKey("playerId")) {
            player = em.getReference(Player.class, ((Number) data.get("playerId")).longValue());
        }

        GameLineupPlayer.Side teamSide = null;
        if (data.containsKey("team")) {
            try {
                teamSide = GameLineupPlayer.Side.valueOf(data.get("team").toString().toUpperCase());
            } catch (Exception ignored) {}
        }

        String meta = "";
        try {
            meta = mapper.writeValueAsString(data);
        } catch (Exception ignored) {}

        GameEvent event = GameEvent.builder()
                .game(game)
                .team(team)
                .player(player)
                .period((Integer) data.getOrDefault("period", 1))
                .clockTime((String) data.getOrDefault("clockTime", null))
                .teamSide(teamSide)
                .meta(meta)
                .ts(LocalDateTime.now())
                .type(type)
                .build();

        return gameEventRepository.save(event);
    }

    /** 3. 샷클락 리셋 (24초) */
    public GameEvent recordShotclockReset(Long gameId, Map<String, Object> data) {
        data.put("seconds", 24);
        return saveEvent(gameId, GameEvent.EventType.SHOTCLOCK_RESET, data);
    }

    /** 4. 샷클락 리셋 (14초) */
    public GameEvent recordShotclockReset14(Long gameId, Map<String, Object> data) {
        data.put("seconds", 14);
        return saveEvent(gameId, GameEvent.EventType.SHOTCLOCK_RESET, data);
    }

    /** 5. 득점 */
    public GameEvent recordScore(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.SCORE, data);
    }

    /** 7. 파울 */
    public GameEvent recordFoul(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.FOUL, data);
    }

    /** 8. 소유권 변경 */
    public GameEvent recordPossession(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.POSSESSION_CHANGE, data);
    }

    /** 9. 교체 */
    public GameEvent recordSubstitution(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.SUBSTITUTION, data);
    }

    /** 10. 타임아웃 */
    public GameEvent recordTimeout(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.TIMEOUT, data);
    }

    /** 11. 쿼터 시작 */
    public GameEvent recordPeriodStart(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.PERIOD_START, data);
    }

    /** 12. 쿼터 종료 */
    public GameEvent recordPeriodEnd(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.PERIOD_END, data);
    }

    /** 2. 시계 변경 */
    public GameEvent recordClock(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.CLOCK_UPDATE, data);
    }

    /** 13. 메모 추가 */
    public GameEvent recordNote(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.NOTE, data);
    }

    /** 15. 경기 종료 */
    public GameEvent recordGameFinish(Long gameId, Map<String, Object> data) {
        return saveEvent(gameId, GameEvent.EventType.GAME_FINISH, data);
    }

    /** 브로드캐스트용 이벤트 포맷 */
    public Map<String, Object> toPbpEvent(GameEvent ev) {
        return Map.of(
                "type", "event",
                "action", "pbp.append",
                "data", Map.of(
                        "eventId", ev.getId(),
                        "type", ev.getType().name(),
                        "team", ev.getTeamSide() != null ? ev.getTeamSide().name() : null,
                        "playerId", ev.getPlayer() != null ? ev.getPlayer().getId() : null,
                        "meta", ev.getMeta()
                )
        );
    }
}
