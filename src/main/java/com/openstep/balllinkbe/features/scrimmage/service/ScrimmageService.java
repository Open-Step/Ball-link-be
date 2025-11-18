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
    private final PlayerRepository playerRepo; // 선수 엔티티 필요

    // 인메모리 라인업 저장 (DB 영향 없이)
    private final Map<Long, List<ScrimmageDetailResponse.PlayerLineup>> guestMap = new ConcurrentHashMap<>();

    /** 🔹 원샷 처리 (자체전 생성 + 엔트리 저장 + 세션발급) */
    @Transactional
    public InitiateScrimmageResponse initiateScrimmage(InitiateScrimmageRequest req, User currentUser) {
        // 1️⃣ 자체전 생성
        var gameId = createScrimmage(
                new CreateScrimmageRequest(req.getHomeTeamId(), req.getAwayTeamId()),
                currentUser
        );

        // 2️⃣ 엔트리 저장
        var entryReq = new AddEntryRequest(req.getHomePlayers(), req.getAwayPlayers());
        saveEntries(gameId, entryReq, currentUser);

        // 3️⃣ 세션 발급
        String sessionToken = createScoreSession(gameId, currentUser);

        return new InitiateScrimmageResponse(gameId, sessionToken);
    }

    /** 라인업 저장 */
    @Transactional
    public void saveEntries(Long gameId, AddEntryRequest req, User currentUser) {

        // 기존 라인업 삭제
        lineupRepo.deleteByGameId(gameId);

        Game game = gameRepository.getReferenceById(gameId);

        var list = new ArrayList<GameLineupPlayer>();

        // HOME
        if (req.getHomePlayers() != null) {
            for (var p : req.getHomePlayers()) {

                Player player;

                if (p.getPlayerId() != null) {
                    // 기존 팀 선수
                    player = playerRepo.getReferenceById(p.getPlayerId());
                } else {
                    // 게스트 선수 → 새로운 Player 엔티티 생성
                    player = Player.builder()
                            .team(game.getHomeTeam())
                            .name(p.getName())
                            .number(
                                    p.getPlayerId() != null && p.getNumber() != null
                                            ? p.getNumber().shortValue()
                                            : null
                            )
                            .isActive(true)
                            .position(p.getPosition() != null
                                    ? Position.valueOf(p.getPosition().toUpperCase())
                                    : null)
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
                            .number(
                                    p.getPlayerId() != null && p.getNumber() != null
                                            ? p.getNumber().shortValue()
                                            : null
                            )
                            .isActive(true)
                            .position(p.getPosition() != null
                                    ? Position.valueOf(p.getPosition().toUpperCase())
                                    : null)
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

    /** 자체전 생성 */
    @Transactional
    public Long createScrimmage(CreateScrimmageRequest req, User currentUser) {
        Team home = teamRepository.findById(req.getHomeTeamId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        Team away = teamRepository.findById(req.getAwayTeamId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

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

    /** 게스트 추가 */
    public Long addGuest(Long gameId, CreateGuestRequest req, User currentUser) {
        var guests = guestMap.computeIfAbsent(gameId, __ -> new java.util.ArrayList<>());
        long guestId = System.currentTimeMillis();
        guests.add(ScrimmageDetailResponse.PlayerLineup.builder()
                .playerId(guestId)
                .name(req.getName())
                .number(req.getNumber())
                .position(req.getPosition())
                .starter(false)
                .guest(true)
                .teamSide("HOME") // 기본 HOME으로 처리 (필요시 프론트에서 지정 가능)
                .build());
        return guestId;
    }

    /** 상세 조회 */
    public ScrimmageDetailResponse getScrimmageDetail(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        var lineup = guestMap.getOrDefault(gameId, List.of());
        var home = lineup.stream()
                .filter(p -> "HOME".equalsIgnoreCase(p.getTeamSide()))
                .toList();
        var away = lineup.stream()
                .filter(p -> "AWAY".equalsIgnoreCase(p.getTeamSide()))
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

    /** 스코어 세션 생성 */
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
                .expiresAt(null) // 🔥 ACTIVE 로 인식되도록 설정
                .build();

        scoreSessionRepository.save(session);
        return session.getSessionToken();
    }



    /** 스코어 세션 조회 */
    @Transactional(readOnly = true)
    public Map<String, Object> getScoreSession(Long gameId) {
        var session = scoreSessionRepository.findByGameId(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        return Map.of(
                "token", session.getSessionToken(),
                "createdAt", session.getCreatedAt(),
                "isActive", session.getExpiresAt().isAfter(LocalDateTime.now())
        );
    }


    /** 종료 */
    @Transactional
    public void endScrimmage(Long gameId, User currentUser) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));
        if (!game.isScrimmage()) throw new CustomException(ErrorCode.INVALID_GAME_TYPE);

        game.setState(Game.State.FINISHED);
        game.setFinishedAt(LocalDateTime.now());
    }
    public List<ScrimmageDetailResponse.PlayerLineup> getLineupRaw(Long gameId) {
        return guestMap.getOrDefault(gameId, List.of());
    }

}
