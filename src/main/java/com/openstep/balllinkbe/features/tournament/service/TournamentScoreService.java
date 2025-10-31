package com.openstep.balllinkbe.features.tournament.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.score.ScoreSession;
import com.openstep.balllinkbe.domain.tournament.Tournament;
import com.openstep.balllinkbe.domain.tournament.TournamentEntry;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
import com.openstep.balllinkbe.features.tournament.dto.request.AddEntryRequest;
import com.openstep.balllinkbe.features.tournament.dto.request.InitiateTournamentRequest;
import com.openstep.balllinkbe.features.score.repository.ScoreSessionRepository;
import com.openstep.balllinkbe.features.tournament.repository.TournamentEntryV2Repository;
import com.openstep.balllinkbe.features.tournament.repository.TournamentRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TournamentScoreService {

    private final GameRepository gameRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentEntryV2Repository tournamentEntryRepository;
    private final ScoreSessionRepository scoreSessionRepository;

    @Transactional
    public Map<String, Object> initiateGame(Long tournamentId, Long gameId,
                                            InitiateTournamentRequest req, User currentUser) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new CustomException(ErrorCode.TOURNAMENT_NOT_FOUND));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        removeEntriesForTeams(tournamentId, game);

        List<TournamentEntry> entries = req.toEntities(tournament, game);
        tournamentEntryRepository.saveAll(entries);

        String token = createScoreSession(tournamentId, gameId, currentUser);
        return Map.of("gameId", gameId, "sessionToken", token);
    }

    @Transactional
    public void saveEntries(Long tournamentId, Long gameId,
                            AddEntryRequest req, User currentUser) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        removeEntriesForTeams(tournamentId, game);

        List<TournamentEntry> entries = req.toEntities(game.getTournament(), game);
        tournamentEntryRepository.saveAll(entries);
    }

    @Transactional
    public String createScoreSession(Long tournamentId, Long gameId, User currentUser) {
        var existing = scoreSessionRepository.findByGameId(gameId);
        if (existing.isPresent()) return existing.get().getToken();

        var session = ScoreSession.builder()
                .gameId(gameId)
                .tournamentId(tournamentId)
                .createdBy(currentUser)
                .token("T-" + gameId + "-" + System.currentTimeMillis())
                .createdAt(LocalDateTime.now())
                .build();

        scoreSessionRepository.save(session);
        return session.getToken();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getScoreSession(Long tournamentId, Long gameId) {
        var session = scoreSessionRepository.findByGameId(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));
        return Map.of(
                "token", session.getToken(),
                "createdAt", session.getCreatedAt(),
                "isActive", session.getEndedAt() == null
        );
    }

    @Transactional
    public void endGame(Long tournamentId, Long gameId, User currentUser) {
        var session = scoreSessionRepository.findByGameId(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));
        session.setEndedAt(LocalDateTime.now());
        scoreSessionRepository.save(session);

        var game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));
        game.setState(Game.State.FINISHED);
        game.setFinishedAt(LocalDateTime.now());
        gameRepository.save(game);
    }

    private void removeEntriesForTeams(Long tournamentId, Game game) {
        var all = tournamentEntryRepository.findByTournamentId(tournamentId);
        Long homeId = game.getHomeTeam() != null ? game.getHomeTeam().getId() : null;
        Long awayId = game.getAwayTeam() != null ? game.getAwayTeam().getId() : null;

        var toDelete = all.stream()
                .filter(e ->
                        (homeId != null && e.getTeam() != null && homeId.equals(e.getTeam().getId())) ||
                                (awayId != null && e.getTeam() != null && awayId.equals(e.getTeam().getId()))
                )
                .toList();

        if (!toDelete.isEmpty()) {
            tournamentEntryRepository.deleteAllInBatch(toDelete);
        }
    }
}
