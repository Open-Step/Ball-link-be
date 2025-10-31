package com.openstep.balllinkbe.features.tournament.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.tournament.dto.request.AddEntryRequest;
import com.openstep.balllinkbe.features.tournament.dto.request.InitiateTournamentRequest;
import com.openstep.balllinkbe.features.tournament.service.TournamentScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tournaments")
@RequiredArgsConstructor
@Tag(name = "tournament-score-controller", description = "대회 경기 스코어보드 API")
public class TournamentScoreController {

    private final TournamentScoreService tournamentScoreService;

    /** 대회 경기 원샷 생성 (엔트리 + 세션) */
    @Operation(summary = "대회 경기 원샷 생성", description = "홈/어웨이 엔트리 저장 및 스코어세션 생성까지 한 번에 수행합니다.")
    @PostMapping("/{tournamentId}/games/{gameId}/initiate")
    public ResponseEntity<?> initiate(
            @PathVariable Long tournamentId,
            @PathVariable Long gameId,
            @RequestBody InitiateTournamentRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        var res = tournamentScoreService.initiateGame(tournamentId, gameId, req, currentUser);
        return ResponseEntity.ok(res); // { gameId, sessionToken }
    }

    /** 엔트리 원샷 등록 */
    @Operation(summary = "대회 경기 엔트리 등록", description = "홈/어웨이 팀 엔트리를 등록합니다.")
    @PostMapping("/{tournamentId}/games/{gameId}/entries")
    public ResponseEntity<?> saveEntries(
            @PathVariable Long tournamentId,
            @PathVariable Long gameId,
            @RequestBody AddEntryRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        tournamentScoreService.saveEntries(tournamentId, gameId, req, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 스코어세션 생성 */
    @Operation(summary = "대회 경기 스코어세션 생성", description = "ScoreManager 접속용 세션 토큰을 발급합니다.")
    @PostMapping("/{tournamentId}/games/{gameId}/session")
    public ResponseEntity<?> createSession(
            @PathVariable Long tournamentId,
            @PathVariable Long gameId,
            @AuthenticationPrincipal User currentUser
    ) {
        var token = tournamentScoreService.createScoreSession(tournamentId, gameId, currentUser);
        return ResponseEntity.ok(Map.of("sessionToken", token));
    }

    /** 스코어세션 상태 조회 */
    @Operation(summary = "대회 경기 스코어세션 조회", description = "현재 경기의 세션 정보를 조회합니다.")
    @GetMapping("/{tournamentId}/games/{gameId}/session")
    public ResponseEntity<?> getSession(
            @PathVariable Long tournamentId,
            @PathVariable Long gameId
    ) {
        var info = tournamentScoreService.getScoreSession(tournamentId, gameId);
        return ResponseEntity.ok(info);
    }

    /** 경기 종료 */
    @Operation(summary = "대회 경기 종료", description = "경기 스코어세션을 종료하고 결과를 확정합니다.")
    @PostMapping("/{tournamentId}/games/{gameId}:end")
    public ResponseEntity<?> end(
            @PathVariable Long tournamentId,
            @PathVariable Long gameId,
            @AuthenticationPrincipal User currentUser
    ) {
        tournamentScoreService.endGame(tournamentId, gameId, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
