package com.openstep.balllinkbe.features.tournament.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.tournament.TournamentEntry;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
import com.openstep.balllinkbe.features.team_manage.repository.PlayerRepository;
import com.openstep.balllinkbe.features.tournament.repository.TournamentEntryV2Repository;
import com.openstep.balllinkbe.features.tournament.dto.request.AddEntryRequest;
import com.openstep.balllinkbe.features.tournament.dto.response.EntryResponse;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class TournamentEntryService {

    private final TournamentEntryV2Repository entryRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public List<EntryResponse> getEntries(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        List<TournamentEntry> entries = entryRepository.findByTournamentId(game.getTournament().getId());
        return entries.stream().map(EntryResponse::new).toList();
    }

    public void updateEntries(Long gameId, List<AddEntryRequest> reqList) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        entryRepository.deleteByTournamentId(game.getTournament().getId());

        for (AddEntryRequest req : reqList) {
            Player player = playerRepository.findById(req.getPlayerId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PLAYER_NOT_FOUND));

            TournamentEntry entry = TournamentEntry.builder()
                    .tournament(game.getTournament())
                    .team(player.getTeam())
                    .player(player)
                    .number(req.getNumber())
                    .position(req.getPosition())
                    .locked(false)
                    .build();

            entryRepository.save(entry);
        }
    }
}
