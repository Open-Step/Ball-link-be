package com.openstep.balllinkbe.features.game.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.features.game.dto.response.GameSummaryResponse;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
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

    public List<GameSummaryResponse> getTournamentGames(Long tournamentId) {
        List<Game> games = gameRepository.findByTournamentIdOrderByScheduledAtAsc(tournamentId);
        if (games.isEmpty()) throw new CustomException(ErrorCode.GAME_NOT_FOUND);
        return games.stream().map(GameSummaryResponse::new).toList();
    }
}
