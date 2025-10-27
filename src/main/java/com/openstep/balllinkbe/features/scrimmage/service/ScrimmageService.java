package com.openstep.balllinkbe.features.scrimmage.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.scrimmage.dto.request.AddEntryRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.request.CreateGuestRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.request.CreateScrimmageRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.response.ScrimmageDetailResponse;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ScrimmageService {

    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;

    // ⚡ 인메모리 게스트 저장 (DB 영향 없이)
    private final Map<Long, List<ScrimmageDetailResponse.PlayerLineup>> guestMap = new ConcurrentHashMap<>();

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

    /** 라인업 저장 (DB 또는 메모리 반영) */
    @Transactional
    public void saveEntries(Long gameId, AddEntryRequest req, User currentUser) {
        // 현재는 DB 영향 없이 메모리 상에만 저장
        var allPlayers = new java.util.ArrayList<ScrimmageDetailResponse.PlayerLineup>();
        req.getHomePlayers().forEach(p -> allPlayers.add(
                ScrimmageDetailResponse.PlayerLineup.builder()
                        .playerId(p.getPlayerId())
                        .name(p.getName())
                        .number(p.getNumber())
                        .position(p.getPosition())
                        .starter(p.isStarter())
                        .guest(false)
                        .build()));
        req.getAwayPlayers().forEach(p -> allPlayers.add(
                ScrimmageDetailResponse.PlayerLineup.builder()
                        .playerId(p.getPlayerId())
                        .name(p.getName())
                        .number(p.getNumber())
                        .position(p.getPosition())
                        .starter(p.isStarter())
                        .guest(false)
                        .build()));
        guestMap.put(gameId, allPlayers);
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
                .build());
        return guestId;
    }

    /** 상세 조회 */
    public ScrimmageDetailResponse getScrimmageDetail(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        var lineup = guestMap.getOrDefault(gameId, List.of());
        var home = lineup.stream().filter(p -> !p.isGuest()).toList();
        var away = lineup.stream().filter(p -> p.isGuest()).toList();

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
    public String createScoreSession(Long gameId, User currentUser) {
        return "SCR-" + gameId + "-" + System.currentTimeMillis();
    }

    /** 스코어 세션 조회 */
    public Map<String, Object> getScoreSession(Long gameId) {
        return Map.of("active", true, "gameId", gameId);
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
}
