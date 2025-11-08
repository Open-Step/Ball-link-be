package com.openstep.balllinkbe.features.score.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.features.team_manage.repository.PlayerRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PlayerResolver {

    private final EntityManager em;
    private final PlayerRepository playerRepository;

    /** 득점/파울/리바운드/어시스트 등번호 → playerId */
    public Map<String, Object> enrichWithPlayerIds(Long gameId, Map<String, Object> data) {
        if (data == null) return Map.of();

        Game game = em.find(Game.class, gameId);
        if (game == null) return data;

        String teamSide = String.valueOf(data.getOrDefault("team", "HOME"));
        Team team = "HOME".equalsIgnoreCase(teamSide) ? game.getHomeTeam() : game.getAwayTeam();

        var out = new LinkedHashMap<>(data);

        // 주 득점자
        if (data.get("number") != null) {
            int num = Integer.parseInt(data.get("number").toString());
            playerRepository.findByTeamIdAndNumberAndIsActiveTrue(team.getId(), (short) num)
                    .ifPresent(p -> out.put("playerId", p.getId()));
        }

        // 어시스트
        if (data.get("assistNumber") != null) {
            int num = Integer.parseInt(data.get("assistNumber").toString());
            playerRepository.findByTeamIdAndNumberAndIsActiveTrue(team.getId(), (short) num)
                    .ifPresent(p -> out.put("assistId", p.getId()));
        }

        // 리바운드
        if (data.get("reboundNumber") != null) {
            int num = Integer.parseInt(data.get("reboundNumber").toString());
            playerRepository.findByTeamIdAndNumberAndIsActiveTrue(team.getId(), (short) num)
                    .ifPresent(p -> out.put("reboundId", p.getId()));
        }

        return out;
    }

    /** 교체: 등번호 → playerId 매핑 */
    public Map<String, Object> enrichSubstitution(Long gameId, Map<String, Object> data) {
        if (data == null) return Map.of();
        Game game = em.find(Game.class, gameId);
        if (game == null) return data;

        String teamSide = String.valueOf(data.getOrDefault("team", "HOME"));
        Team team = "HOME".equalsIgnoreCase(teamSide) ? game.getHomeTeam() : game.getAwayTeam();

        var out = new LinkedHashMap<>(data);
        if (data.get("outNumber") != null) {
            int n = Integer.parseInt(data.get("outNumber").toString());
            playerRepository.findByTeamIdAndNumberAndIsActiveTrue(team.getId(), (short) n)
                    .ifPresent(p -> out.put("outPlayerId", p.getId()));
        }
        if (data.get("inNumber") != null) {
            int n = Integer.parseInt(data.get("inNumber").toString());
            playerRepository.findByTeamIdAndNumberAndIsActiveTrue(team.getId(), (short) n)
                    .ifPresent(p -> out.put("inPlayerId", p.getId()));
        }
        return out;
    }
}
