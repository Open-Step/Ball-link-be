package com.openstep.balllinkbe.features.tournament.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameLineupPlayer;
import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.enums.Position;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
import com.openstep.balllinkbe.features.team_manage.repository.PlayerRepository;
import com.openstep.balllinkbe.features.tournament.dto.request.AddTournamentTeamRequest;
import com.openstep.balllinkbe.features.tournament.repository.GameLineupPlayerRepository;
import com.openstep.balllinkbe.features.tournament.repository.TournamentEntryV2Repository;
import com.openstep.balllinkbe.features.tournament.dto.response.EntryResponse;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TournamentEntryService {

    @PersistenceContext
    private final EntityManager em;

    private final TournamentEntryV2Repository entryRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameLineupPlayerRepository lineupRepository;

    public List<EntryResponse> getEntries(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        var entries = entryRepository.findByTournamentId(game.getTournament().getId());
        return entries.stream().map(EntryResponse::new).toList();
    }

    // ⛳️ 여기 시그니처를 List<AddTournamentTeamRequest>로 변경
    public void updateEntries(Long gameId, List<AddTournamentTeamRequest> reqList) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        // 기존 라인업 전체 삭제 후 재등록
        lineupRepository.deleteByGameId(gameId);

        for (AddTournamentTeamRequest req : reqList) {
            GameLineupPlayer.Side side = GameLineupPlayer.Side.valueOf(req.getTeamSide().toUpperCase());
            Team targetTeam = (side == GameLineupPlayer.Side.HOME) ? game.getHomeTeam() : game.getAwayTeam();

            for (AddTournamentTeamRequest.EntryPlayerDto p : req.getPlayers()) {
                GameLineupPlayer lineup = GameLineupPlayer.builder()
                        .game(game)
                        .team(targetTeam)
                        .teamSide(side)
                        .isStarter(p.isStarter())
                        .number(p.getNumber() != null ? p.getNumber().shortValue() : null)
                        .position(p.getPosition() != null ? Position.valueOf(p.getPosition()) : null)
                        .build();

                if (p.getPlayerId() != null) {
                    Player player = em.find(Player.class, p.getPlayerId());
                    lineup.setPlayer(player);
                } else {
                    lineup.setGuestName(p.getName());
                    lineup.setGuestNumber(p.getNumber() != null ? p.getNumber().shortValue() : null);
                }

                lineupRepository.save(lineup);
            }
        }
    }
}
