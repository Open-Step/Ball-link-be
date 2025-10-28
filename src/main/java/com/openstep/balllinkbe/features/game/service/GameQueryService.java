package com.openstep.balllinkbe.features.game.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameTeamStat;
import com.openstep.balllinkbe.features.game.dto.response.GameSummaryResponse;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
import com.openstep.balllinkbe.features.game.repository.GameTeamStatRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GameQueryService {

    private final GameRepository gameRepository;
    private final GameTeamStatRepository gameTeamStatRepository;

    public List<GameSummaryResponse> getTournamentGames(Long tournamentId) {
        List<Game> games = gameRepository.findByTournamentIdOrderByScheduledAtAsc(tournamentId);

        return games.stream().map(game -> {
            Integer homeScore = null;
            Integer awayScore = null;

            if (game.getHomeTeam() != null) {
                homeScore = gameTeamStatRepository.findByGameIdAndTeamId(game.getId(), game.getHomeTeam().getId())
                        .map(GameTeamStat::getPts)
                        .orElse(0);
            }

            if (game.getAwayTeam() != null) {
                awayScore = gameTeamStatRepository.findByGameIdAndTeamId(game.getId(), game.getAwayTeam().getId())
                        .map(GameTeamStat::getPts)
                        .orElse(0);
            }

            return new GameSummaryResponse(game, homeScore, awayScore);
        }).toList();
    }}
