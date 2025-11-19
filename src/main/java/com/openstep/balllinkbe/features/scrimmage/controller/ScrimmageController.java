package com.openstep.balllinkbe.features.scrimmage.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.scrimmage.dto.request.InitiateScrimmageRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.request.AddEntryRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.request.CreateGuestRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.request.CreateScrimmageRequest;
import com.openstep.balllinkbe.features.scrimmage.service.ScrimmageService;
import com.openstep.balllinkbe.features.scrimmage.dto.response.ScrimmageDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/v1/scrimmages")
@RequiredArgsConstructor
@Tag(name = "scrimmage-controller", description = "자체전(스크림) 경기 API")
public class ScrimmageController {

    private final ScrimmageService scrimmageService;

    /** 자체전 원샷 생성 (팀 생성 + 경기 생성 + 엔트리 + 세션) */
    @Operation(
            summary = "자체전 생성(원샷)",
            description = """
                    자체전 전용 API입니다.

                    - 홈팀/원정팀 이름을 입력하면 백엔드가 자동으로 가상 팀을 생성합니다.
                    - 팀 ID는 전달하지 않으며 DB에서 자동 발급됩니다.
                    - 엔트리(라인업)를 함께 등록합니다.
                    - 스코어보드 접속을 위한 세션 토큰을 바로 발급합니다.

                    요청 예:
                    {
                      "homeTeamName": "A팀",
                      "awayTeamName": "B팀",
                      "homePlayers": [...],
                      "awayPlayers": [...]
                    }
                    """
    )
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateScrimmage(
            @RequestBody InitiateScrimmageRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        var response = scrimmageService.initiateScrimmage(req, currentUser);
        return ResponseEntity.ok(response);
    }

    /** 자체전 게임만 생성 */
    @Operation(
            summary = "자체전 게임 생성",
            description = """
                    자체전 전용 게임을 생성합니다.

                    - 홈팀/원정팀 이름을 입력하면 백엔드가 자동으로 가상 팀을 생성합니다.
                    - 생성된 gameId를 반환합니다.
                    - 엔트리 저장은 별도의 API(`/entries`)로 수행합니다.

                    요청 예:
                    {
                      "homeTeamName": "A팀",
                      "awayTeamName": "B팀"
                    }
                    """
    )
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody CreateScrimmageRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        Long gameId = scrimmageService.createScrimmage(req, currentUser);
        return ResponseEntity.ok(Map.of("gameId", gameId));
    }

    /** 자체전 라인업 저장 */
    @Operation(
            summary = "자체전 라인업 저장",
            description = """
                    홈/어웨이 팀의 엔트리(선수 목록)를 저장하거나 수정합니다.

                    - 선수가 기존 팀 소속이 아니면 자동으로 해당 가상 팀에 Player 엔티티가 생성됩니다.
                    - playerId 가 null 이면 '게스트 선수'로 처리됩니다.
                    """
    )
    @PostMapping("/{gameId}/entries")
    public ResponseEntity<?> saveEntries(
            @PathVariable Long gameId,
            @RequestBody AddEntryRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        scrimmageService.saveEntries(gameId, req, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 게스트 선수 추가 */
    @Operation(
            summary = "게스트 선수 추가",
            description = """
                    자체전에서 비회원(게스트) 선수를 추가합니다.

                    - 게스트는 DB에 Player 엔티티가 생성되지 않고 인메모리로 관리됩니다.
                    - playerId 대신 guestId(임시 long 값)를 반환합니다.
                    - teamSide 는 기본값이 HOME이며, 필요하면 요청에서 지정할 수 있습니다.
                    """
    )
    @PostMapping("/{gameId}/guests")
    public ResponseEntity<?> addGuest(
            @PathVariable Long gameId,
            @RequestBody CreateGuestRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        var guestId = scrimmageService.addGuest(gameId, req, currentUser);
        return ResponseEntity.ok(Map.of("guestId", guestId));
    }

    /** 자체전 상세조회 */
    @Operation(
            summary = "자체전 상세조회",
            description = """
                    자체전 경기의 기본 정보와 엔트리(라인업)를 조회합니다.

                    - ScoreBoard/ScoreManager가 최초 진입할 때 사용하는 API 입니다.
                    - 자체전이므로 homeTeam / awayTeam 은 백엔드가 생성한 가상 팀입니다.
                    """
    )
    @GetMapping("/{gameId}")
    public ResponseEntity<ScrimmageDetailResponse> getDetail(@PathVariable Long gameId) {
        var detail = scrimmageService.getScrimmageDetail(gameId);
        return ResponseEntity.ok(detail);
    }

    /** 스코어 세션 생성 */
    @Operation(
            summary = "스코어세션 생성",
            description = """
                    스코어보드(ScoreManager) 접속을 위한 세션 토큰을 발급합니다.

                    - 이미 존재하는 세션이 있으면 재발급하지 않고 기존 토큰을 반환합니다.
                    """
    )
    @PostMapping("/{gameId}/session")
    public ResponseEntity<?> createSession(
            @PathVariable Long gameId,
            @AuthenticationPrincipal User currentUser
    ) {
        var token = scrimmageService.createScoreSession(gameId, currentUser);
        return ResponseEntity.ok(Map.of("sessionToken", token));
    }

    /** 스코어 세션 조회 */
    @Operation(
            summary = "스코어세션 조회",
            description = """
                    해당 경기에 대한 세션 토큰 상태를 조회합니다.

                    반환 예:
                    {
                      "token": "...",
                      "createdAt": "...",
                      "isActive": true
                    }
                    """
    )
    @GetMapping("/{gameId}/session")
    public ResponseEntity<?> getSession(@PathVariable Long gameId) {
        var info = scrimmageService.getScoreSession(gameId);
        return ResponseEntity.ok(info);
    }

    /** 자체전 종료 */
    @Operation(
            summary = "자체전 종료",
            description = """
                    자체전 경기를 종료 상태(FINISHED)로 변경합니다.
                    종료 시점이 기록됩니다.
                    """
    )
    @PostMapping("/{gameId}:end")
    public ResponseEntity<?> end(
            @PathVariable Long gameId,
            @AuthenticationPrincipal User currentUser
    ) {
        scrimmageService.endScrimmage(gameId, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
