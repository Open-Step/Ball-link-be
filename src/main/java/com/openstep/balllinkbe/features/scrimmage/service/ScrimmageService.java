package com.openstep.balllinkbe.features.scrimmage.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameLineupPlayer;
import com.openstep.balllinkbe.domain.score.ScoreSession;
import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.enums.Position;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.score.repository.ScoreSessionRepository;
import com.openstep.balllinkbe.features.scrimmage.dto.request.*;
import com.openstep.balllinkbe.features.scrimmage.dto.response.InitiateScrimmageResponse;
import com.openstep.balllinkbe.features.scrimmage.dto.response.ScrimmageDetailResponse;
import com.openstep.balllinkbe.features.team_manage.repository.PlayerRepository;
import com.openstep.balllinkbe.features.tournament.repository.GameLineupPlayerRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ScrimmageService {

    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;
    private final ScoreSessionRepository scoreSessionRepository;
    private final GameLineupPlayerRepository lineupRepo;
    private final PlayerRepository playerRepo;

    private final Map<Long, List<ScrimmageDetailResponse.PlayerLineup>> guestMap = new ConcurrentHashMap<>();


    /** ✔ 자체전 원샷 처리 (팀 생성 + 게임 생성 + 엔트리 저장 + 세션발급) */
    @Transactional
    public InitiateScrimmageResponse initiateScrimmage(InitiateScrimmageRequest req, User currentUser) {

        // 1) 자체전용 게임 생성 (가상팀 포함)
        Long gameId = createScrimmage(
                new CreateScrimmageRequest(req.getHomeTeamName(), req.getAwayTeamName()),
                currentUser
        );

        // 2) 엔트리 저장
        var entryReq = new AddEntryRequest(req.getHomePlayers(), req.getAwayPlayers());
        saveEntries(gameId, entryReq, currentUser);

        // 3) 세션 발급
        String sessionToken = createScoreSession(gameId, currentUser);

        return new InitiateScrimmageResponse(gameId, sessionToken);
    }


    /** ✔ 가상 팀 생성 메서드 */
    private Team createVirtualTeam(String teamName, User owner) {
        Team t = Team.builder()
                .name(teamName + " (자체전)")
                .ownerUser(owner)
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .build();
        return teamRepository.save(t);
    }


    /** ✔ 자체전 게임 생성 (가상 팀 2개 자동 생성) */
    @Transactional
    public Long createScrimmage(CreateScrimmageRequest req, User currentUser) {

        Team home = createVirtualTeam(req.getHomeTeamName(), currentUser);
        Team away = createVirtualTeam(req.getAwayTeamName(), currentUser);

        Game game = Game.builder()
                .homeTeam(home)
                .awayTeam(away)
                .isScrimmage(true)
                .state(Game.State.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .build();

        gameRepository.save(game);
        return game.getId();
    }

    public Long addGuest(Long gameId, CreateGuestRequest req, User currentUser) {

        var guests = guestMap.computeIfAbsent(gameId, __ -> new ArrayList<>());

        long guestId = System.currentTimeMillis();

        guests.add(
                ScrimmageDetailResponse.PlayerLineup.builder()
                        .playerId(guestId)
                        .name(req.getName())
                        .number(req.getNumber())
                        .position(req.getPosition())
                        .starter(false)
                        .guest(true)
                        .teamSide(req.getTeamSide() != null ? req.getTeamSide() : "HOME")
                        .build()
        );

        return guestId;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getScoreSession(Long gameId) {
        var session = scoreSessionRepository.findByGameId(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        return Map.of(
                "token", session.getSessionToken(),
                "createdAt", session.getCreatedAt(),
                "isActive", session.getExpiresAt() == null || session.getExpiresAt().isAfter(LocalDateTime.now())
        );
    }

    /** ✔ 라인업 저장 */
    @Transactional
    public void saveEntries(Long gameId, AddEntryRequest req, User currentUser) {

        lineupRepo.deleteByGameId(gameId);

        Game game = gameRepository.getReferenceById(gameId);

        var list = new ArrayList<GameLineupPlayer>();

        // HOME
        if (req.getHomePlayers() != null) {
            for (var p : req.getHomePlayers()) {

                Player player;

                if (p.getPlayerId() != null) {
                    player = playerRepo.getReferenceById(p.getPlayerId());
                } else {
                    player = Player.builder()
                            .team(game.getHomeTeam())
                            .name(p.getName())
                            .number(p.getNumber() != null ? p.getNumber().shortValue() : null)
                            .isActive(true)
                            .position(parsePosition(p.getPosition()))
                            .build();

                    playerRepo.save(player);
                }

                list.add(GameLineupPlayer.builder()
                        .game(game)
                        .team(game.getHomeTeam())
                        .teamSide(GameLineupPlayer.Side.HOME)
                        .player(player)
                        .number(player.getNumber())
                        .position(player.getPosition())
                        .isStarter(p.isStarter())
                        .build());
            }
        }

        // AWAY
        if (req.getAwayPlayers() != null) {
            for (var p : req.getAwayPlayers()) {

                Player player;

                if (p.getPlayerId() != null) {
                    player = playerRepo.getReferenceById(p.getPlayerId());
                } else {
                    player = Player.builder()
                            .team(game.getAwayTeam())
                            .name(p.getName())
                            .number(p.getNumber() != null ? p.getNumber().shortValue() : null)
                            .isActive(true)
                            .position(parsePosition(p.getPosition()))
                            .build();
                    playerRepo.save(player);
                }

                list.add(GameLineupPlayer.builder()
                        .game(game)
                        .team(game.getAwayTeam())
                        .teamSide(GameLineupPlayer.Side.AWAY)
                        .player(player)
                        .number(player.getNumber())
                        .position(player.getPosition())
                        .isStarter(p.isStarter())
                        .build());
            }
        }

        lineupRepo.saveAll(list);
    }


    private Position parsePosition(String pos) {
        if (pos == null) return null;
        pos = pos.trim();
        if (pos.isEmpty()) return null;

        try {
            return Position.valueOf(pos.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }


    /** ✔ 세션 생성 */
    @Transactional
    public String createScoreSession(Long gameId, User currentUser) {

        var existing = scoreSessionRepository.findByGameId(gameId);
        if (existing.isPresent()) {
            return existing.get().getSessionToken();
        }

        var session = ScoreSession.builder()
                .gameId(gameId)
                .createdBy(currentUser)
                .sessionToken("SCR-" + gameId + "-" + System.currentTimeMillis())
                .createdAt(LocalDateTime.now())
                .expiresAt(null)
                .build();

        scoreSessionRepository.save(session);
        return session.getSessionToken();
    }


    /** 상세 조회 (기존 유지) */
    public ScrimmageDetailResponse getScrimmageDetail(Long gameId) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        List<GameLineupPlayer> dbLineup = lineupRepo.findByGameId(gameId);

        var home = dbLineup.stream()
                .filter(p -> p.getTeamSide() == GameLineupPlayer.Side.HOME)
                .map(this::convertLineup)
                .toList();

        var away = dbLineup.stream()
                .filter(p -> p.getTeamSide() == GameLineupPlayer.Side.AWAY)
                .map(this::convertLineup)
                .toList();

        return ScrimmageDetailResponse.builder()
                .gameId(game.getId())
                .homeTeamName(game.getHomeTeam().getName())
                .awayTeamName(game.getAwayTeam().getName())
                .venueName(game.getVenue() != null ? game.getVenue().getName() : null)
                .state(game.getState().name())
                .startedAt(game.getStartedAt())
                .homeLineup(home)
                .awayLineup(away)
                .build();
    }


    private ScrimmageDetailResponse.PlayerLineup convertLineup(GameLineupPlayer p) {

        boolean isGuest = (p.getPlayer() == null);

        Integer number = isGuest
                ? (p.getGuestNumber() != null ? p.getGuestNumber().intValue() : null)
                : (p.getNumber() != null ? p.getNumber().intValue() : null);

        return ScrimmageDetailResponse.PlayerLineup.builder()
                .playerId(isGuest ? null : p.getPlayer().getId())
                .name(isGuest ? p.getGuestName() : p.getPlayer().getName())
                .number(number)
                .position(p.getPosition() != null ? p.getPosition().name() : null)
                .starter(p.isStarter())
                .guest(isGuest)
                .teamSide(p.getTeamSide().name())
                .build();
    }


    /** 종료 */
    @Transactional
    public void endScrimmage(Long gameId, User currentUser) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        if (!game.isScrimmage())
            throw new CustomException(ErrorCode.INVALID_GAME_TYPE);

        game.setState(Game.State.FINISHED);
        game.setFinishedAt(LocalDateTime.now());
    }
}
