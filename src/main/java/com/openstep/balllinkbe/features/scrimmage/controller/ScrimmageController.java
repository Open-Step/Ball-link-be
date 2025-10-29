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

    /** 자체전 원샷 생성 (생성 + 엔트리 + 세션) */
    @Operation(summary = "자체전 생성(원샷)", description = "홈/어웨이 팀 지정 + 엔트리 저장 + 세션 발급을 한 번에 처리합니다.")
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateScrimmage(
            @RequestBody InitiateScrimmageRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        var response = scrimmageService.initiateScrimmage(req, currentUser);
        return ResponseEntity.ok(response); // { gameId, sessionToken }
    }

    /** 자체전 생성 */
    @Operation(summary = "자체전 생성", description = "홈/원정 팀을 지정하여 자체전을 생성합니다.")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateScrimmageRequest req,
                                    @AuthenticationPrincipal User currentUser) {
        Long gameId = scrimmageService.createScrimmage(req, currentUser);
        return ResponseEntity.ok(Map.of("gameId", gameId));
    }

    /** 자체전 라인업 저장 */
    @Operation(summary = "자체전 라인업 저장", description = "홈/어웨이 팀의 엔트리를 등록하거나 수정합니다.")
    @PostMapping("/{gameId}/entries")
    public ResponseEntity<?> saveEntries(@PathVariable Long gameId, @RequestBody AddEntryRequest req,
                                         @AuthenticationPrincipal User currentUser) {
        scrimmageService.saveEntries(gameId, req, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 게스트 선수 추가 */
    @Operation(summary = "게스트 추가", description = "비회원 선수를 게스트로 추가합니다.")
    @PostMapping("/{gameId}/guests")
    public ResponseEntity<?> addGuest(@PathVariable Long gameId,
                                      @RequestBody CreateGuestRequest req,
                                      @AuthenticationPrincipal User currentUser) {
        var guestId = scrimmageService.addGuest(gameId, req, currentUser);
        return ResponseEntity.ok(Map.of("guestId", guestId));
    }

    /** 자체전 상세조회 */
    @Operation(summary = "자체전 상세조회", description = "ScoreManager에서 사용할 자체전 경기 기본정보 + 라인업을 조회합니다.")
    @GetMapping("/{gameId}")
    public ResponseEntity<ScrimmageDetailResponse> getDetail(@PathVariable Long gameId) {
        var detail = scrimmageService.getScrimmageDetail(gameId);
        return ResponseEntity.ok(detail);
    }

    /** 스코어 세션 생성 (ScoreManager 진입 시) */
    @Operation(summary = "자체전 스코어세션 생성", description = "스코어보드 접속을 위한 세션 토큰을 발급합니다.")
    @PostMapping("/{gameId}/session")
    public ResponseEntity<?> createSession(@PathVariable Long gameId, @AuthenticationPrincipal User currentUser) {
        var token = scrimmageService.createScoreSession(gameId, currentUser);
        return ResponseEntity.ok(Map.of("sessionToken", token));
    }

    /** 스코어 세션 조회 */
    @Operation(summary = "자체전 스코어세션 조회", description = "현재 세션 상태를 조회합니다.")
    @GetMapping("/{gameId}/session")
    public ResponseEntity<?> getSession(@PathVariable Long gameId) {
        var info = scrimmageService.getScoreSession(gameId);
        return ResponseEntity.ok(info);
    }

    /** 자체전 종료 */
    @Operation(summary = "자체전 종료", description = "자체전을 종료합니다.")
    @PostMapping("/{gameId}:end")
    public ResponseEntity<?> end(@PathVariable Long gameId, @AuthenticationPrincipal User currentUser) {
        scrimmageService.endScrimmage(gameId, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
