package com.openstep.balllinkbe.features.team_manage.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.CreatePlayerRequest;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdatePlayerRequest;
import com.openstep.balllinkbe.features.team_manage.dto.response.PlayerResponse;
import com.openstep.balllinkbe.features.team_manage.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/players")
@RequiredArgsConstructor
@Tag(name = "team-player-controller", description = "팀 선수 관리 API")
public class PlayerController {

    private final PlayerService playerService;

    /** 선수 등록 */
    @PostMapping
    @Operation(summary = "선수 등록", description = "팀에 선수를 등록합니다.")
    public ResponseEntity<?> createPlayer(
            @PathVariable Long teamId,
            @RequestBody CreatePlayerRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        Long playerId = playerService.createPlayer(teamId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", playerId));
    }

    /** 선수 목록 조회 */
    @GetMapping
    @Operation(summary = "선수 목록 조회", description = "팀의 선수 목록을 조회합니다.")
    public ResponseEntity<List<PlayerResponse>> getPlayers(@PathVariable Long teamId) {
        return ResponseEntity.ok(playerService.getPlayers(teamId));
    }

    /** 선수 수정 */
    @PatchMapping("/{playerId}")
    @Operation(summary = "선수 수정", description = "선수의 번호/포지션/비고를 수정합니다.")
    public ResponseEntity<?> updatePlayer(
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @RequestBody UpdatePlayerRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        playerService.updatePlayer(teamId, playerId, request, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 선수 삭제 */
    @DeleteMapping("/{playerId}")
    @Operation(summary = "선수 삭제(퇴출)", description = "팀장이 선수를 퇴출합니다.")
    public ResponseEntity<?> deletePlayer(
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @AuthenticationPrincipal User currentUser
    ) {
        playerService.deletePlayer(teamId, playerId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /** 등번호 중복 확인 */
    @GetMapping("/validate-number")
    @Operation(summary = "등번호 중복 확인", description = "팀 또는 대회 단위로 등번호 중복 여부를 확인합니다.")
    public ResponseEntity<Map<String, Boolean>> validateNumber(
            @PathVariable Long teamId,
            @RequestParam Short number,  // int → Short
            @RequestParam(defaultValue = "TEAM") String scope,
            @RequestParam(required = false) Long tournamentId
    ) {
        boolean available = playerService.validateNumber(teamId, number, scope, tournamentId);
        return ResponseEntity.ok(Map.of("available", available));
    }


    /** 멤버를 선수로 편입 */
    @PostMapping("/from-member")
    @Operation(summary = "멤버를 선수로 편입", description = "팀 멤버를 선수로 등록합니다.")
    public ResponseEntity<?> addFromMember(
            @PathVariable Long teamId,
            @RequestBody CreatePlayerRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        Long playerId = playerService.addFromMember(teamId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", playerId));
    }
}
