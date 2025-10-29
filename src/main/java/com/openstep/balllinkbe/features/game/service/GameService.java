package com.openstep.balllinkbe.features.game.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameTeamStat;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.domain.tournament.Tournament;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.domain.venue.Venue;
import com.openstep.balllinkbe.features.game.dto.request.CreateTournamentGameRequest;
import com.openstep.balllinkbe.features.game.dto.request.UpdateGameRequest;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
import com.openstep.balllinkbe.features.game.repository.GameTeamStatRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamMemberRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameTeamStatRepository gameTeamStatRepository;
    private final TeamMemberRepository teamMemberRepository;

    @PersistenceContext
    private EntityManager em;

    /** 대회 경기 생성 */
    @Transactional
    public Long createTournamentGame(Long tournamentId, CreateTournamentGameRequest req, User currentUser) {
        if (req.getHomeTeamId() == null || req.getAwayTeamId() == null || req.getScheduledAt() == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Tournament tournament = em.find(Tournament.class, tournamentId);
        if (tournament == null) throw new CustomException(ErrorCode.TOURNAMENT_NOT_FOUND);

        Team home = em.find(Team.class, req.getHomeTeamId());
        Team away = em.find(Team.class, req.getAwayTeamId());
        if (home == null || away == null) throw new CustomException(ErrorCode.TEAM_NOT_FOUND);

        // venueName으로 생성 or 재사용
        Venue venue = null;
        if (req.getVenueName() != null && !req.getVenueName().isBlank()) {
            var existing = em.createQuery("SELECT v FROM Venue v WHERE v.name = :name", Venue.class)
                    .setParameter("name", req.getVenueName())
                    .getResultList();
            if (!existing.isEmpty()) {
                venue = existing.get(0);
            } else {
                venue = Venue.builder()
                        .name(req.getVenueName())
                        .createdAt(LocalDateTime.now())
                        .build();
                em.persist(venue);
            }
        }

        // round 숫자 → enum 매핑
        Game.RoundCode round = switch (req.getRound() != null ? req.getRound() : 0) {
            case 16 -> Game.RoundCode.ROUND_OF_16;
            case 8 -> Game.RoundCode.QF;
            case 4 -> Game.RoundCode.SF;
            case 2 -> Game.RoundCode.FINAL;
            default -> Game.RoundCode.GROUP;
        };

        Game game = Game.builder()
                .tournament(tournament)
                .homeTeam(home)
                .awayTeam(away)
                .opponentName(null)
                .roundCode(round)
                .bracketOrder(req.getBracketOrder())
                .state(Game.State.SCHEDULED)
                .scheduledAt(req.getScheduledAt())
                .venue(venue)
                .createdAt(LocalDateTime.now())
                .isScrimmage(false)
                .build();

        Game saved = gameRepository.save(game);
        createZeroTeamStat(saved, home);
        createZeroTeamStat(saved, away);

        return saved.getId();
    }

    /** 경기 수정 */
    @Transactional
    public void updateGame(Long gameId, UpdateGameRequest req, User currentUser) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        // 스크림이 아니므로, 권한은 추후 주최자/리그관리자 모델 생기면 적용 (TODO)

        if (req.getScheduledAt() != null) game.setScheduledAt(req.getScheduledAt());
        if (req.getVenueId() != null) {
            Venue v = em.find(Venue.class, req.getVenueId());
            if (v == null) throw new CustomException(ErrorCode.VENUE_NOT_FOUND);
            game.setVenue(v);
        }
        if (req.getRoundCode() != null) game.setRoundCode(Game.RoundCode.valueOf(req.getRoundCode()));
        if (req.getBracketOrder() != null) game.setBracketOrder(req.getBracketOrder());

        gameRepository.save(game);
    }

    private void createZeroTeamStat(Game game, Team team) {
        GameTeamStat stat = GameTeamStat.builder()
                .game(game).team(team)
                .pts(0).reb(0).ast(0).stl(0).blk(0).pf(0).tov(0)
                .build();
        gameTeamStatRepository.save(stat);
    }
}
