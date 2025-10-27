package com.openstep.balllinkbe.features.scrimmage.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.scrimmage.dto.request.CreateScrimmageRequest;
import com.openstep.balllinkbe.features.scrimmage.service.ScrimmageService;
import com.openstep.balllinkbe.features.game.dto.response.GameCreatedResponse;
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

    /** 자체전 생성 */
    @Operation(summary = "자체전 생성", description = "홈/원정 팀을 지정하여 일회성 경기를 생성합니다.")
    @PostMapping
    public ResponseEntity<GameCreatedResponse> create(@RequestBody CreateScrimmageRequest req,
                                                      @AuthenticationPrincipal User currentUser) {
        Long gameId = scrimmageService.createScrimmage(req, currentUser);
        return ResponseEntity.ok(new GameCreatedResponse(gameId));
    }

    /** 자체전 시작 */
    @Operation(summary = "자체전 시작", description = "자체전을 진행 상태로 전환합니다.")
    @PostMapping("/{gameId}:start")
    public ResponseEntity<?> start(@PathVariable Long gameId, @AuthenticationPrincipal User currentUser) {
        scrimmageService.startScrimmage(gameId, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 자체전 종료 */
    @Operation(summary = "자체전 종료", description = "자체전을 종료 상태로 전환합니다.")
    @PostMapping("/{gameId}:end")
    public ResponseEntity<?> end(@PathVariable Long gameId, @AuthenticationPrincipal User currentUser) {
        scrimmageService.endScrimmage(gameId, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
