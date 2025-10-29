package com.openstep.balllinkbe.features.tournament.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameLineupPlayer;
import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.enums.Position;
import com.openstep.balllinkbe.domain.tournament.TournamentEntry;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
import com.openstep.balllinkbe.features.team_manage.repository.PlayerRepository;
import com.openstep.balllinkbe.features.tournament.repository.GameLineupPlayerRepository;
import com.openstep.balllinkbe.features.tournament.repository.TournamentEntryV2Repository;
import com.openstep.balllinkbe.features.tournament.dto.request.AddEntryRequest;
import com.openstep.balllinkbe.features.tournament.dto.response.EntryResponse;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class TournamentEntryService {
    @PersistenceContext
    private EntityManager em;

    private final TournamentEntryV2Repository entryRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameLineupPlayerRepository lineupRepository;

    @Transactional
    public List<EntryResponse> getEntries(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        List<TournamentEntry> entries = entryRepository.findByTournamentId(game.getTournament().getId());
        return entries.stream().map(EntryResponse::new).toList();
    }

    @Transactional
    public void updateEntries(Long gameId, List<AddEntryRequest> reqList) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        // 기존 엔트리 전체 삭제 후 새로 등록
        lineupRepository.deleteByGameId(gameId);

        for (AddEntryRequest req : reqList) {
            GameLineupPlayer.Side side = GameLineupPlayer.Side.valueOf(req.getTeamSide().toUpperCase());
            Team targetTeam = (side == GameLineupPlayer.Side.HOME)
                    ? game.getHomeTeam()
                    : game.getAwayTeam();

            for (AddEntryRequest.EntryPlayerDto p : req.getPlayers()) {
                GameLineupPlayer lineup = GameLineupPlayer.builder()
                        .game(game)
                        .team(targetTeam)
                        .teamSide(side)
                        .isStarter(p.isStarter())
                        .number(p.getNumber() != null ? p.getNumber().shortValue() : null)
                        .position(Position.valueOf(p.getPosition()))
                        .build();

                // 회원선수 or 게스트 구분
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
