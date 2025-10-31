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

/**
 * ⚾️ 팀 선수 관리 컨트롤러
 *
 * - "선수(Player)"는 경기 출전 및 기록 관리용 개체입니다.
 * - 즉, 팀에 소속된 멤버(User)가 실제 경기 로스터로 등록될 때 생성되는 엔터티입니다.
 *
 * 💡 주의:
 *  - "선수 삭제"는 경기/기록상 '선수(Player)' 엔터티를 비활성화하는 것이며,
 *  - "팀원 강퇴"는 '팀 멤버(TeamMember)' 관계 자체를 끊는 별도 API에서 처리합니다.
 *    (예: DELETE /api/v1/teams/{teamId}/members/{userId})
 */
@RestController
@RequestMapping("/api/v1/teams/{teamId}/players")
@RequiredArgsConstructor
@Tag(name = "team-player-controller", description = "팀의 경기용 선수(Player) 관리 API")
public class PlayerController {

    private final PlayerService playerService;

    /** 🧍‍♂️ 선수 등록 (일반 등록)
     * 팀장이 직접 선수 정보를 입력하여 신규 선수를 등록합니다.
     * 예: 외부 게스트 또는 아직 회원이 아닌 인원을 추가할 때 사용.
     */
    @PostMapping
    @Operation(summary = "선수 등록", description = "팀에 새로운 선수를 등록합니다. (회원이 아닌 게스트도 가능)")
    public ResponseEntity<?> createPlayer(
            @PathVariable Long teamId,
            @RequestBody CreatePlayerRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        Long playerId = playerService.createPlayer(teamId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", playerId));
    }

    /** 📋 선수 목록 조회 */
    @GetMapping
    @Operation(summary = "선수 목록 조회", description = "팀의 등록된 선수 목록을 조회합니다. (deletedAt이 null인 선수만 노출)")
    public ResponseEntity<List<PlayerResponse>> getPlayers(@PathVariable Long teamId) {
        return ResponseEntity.ok(playerService.getPlayers(teamId));
    }

    /** ✏️ 선수 수정 */
    @PatchMapping("/{playerId}")
    @Operation(summary = "선수 수정", description = "선수의 번호, 포지션, 비고 등을 수정합니다. (팀장/매니저 권한 필요)")
    public ResponseEntity<?> updatePlayer(
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @RequestBody UpdatePlayerRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        playerService.updatePlayer(teamId, playerId, request, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** ❌ 선수 삭제 (퇴출)
     * 팀장이 경기용 선수(Player) 레코드를 비활성화합니다.
     * - 실제 DB에서 삭제하지 않고 deletedAt, isActive=false로 soft delete 처리.
     * - 주의: 이 API는 팀 소속 관계(TeamMember)를 끊지 않습니다.
     *   → '팀원 강퇴'는 /members/{userId} DELETE API에서 수행.
     */
    @DeleteMapping("/{playerId}")
    @Operation(
            summary = "선수 삭제(퇴출)",
            description = """
        팀장이 경기용 선수(Player) 엔터티를 삭제합니다.
        ⚠️ 주의:
        - 이는 경기/기록용 선수 데이터를 soft delete 하는 것이며,
          팀 멤버(회원)를 강퇴하는 것은 아닙니다.
        - 팀원 강퇴는 별도의 API (/teams/{teamId}/members/{userId})를 사용하세요.
        """
    )
    public ResponseEntity<?> deletePlayer(
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @AuthenticationPrincipal User currentUser
    ) {
        playerService.deletePlayer(teamId, playerId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /** 🔢 등번호 중복 확인 */
    @GetMapping("/validate-number")
    @Operation(summary = "등번호 중복 확인", description = "팀 또는 대회 단위에서 특정 등번호가 이미 사용 중인지 확인합니다.")
    public ResponseEntity<Map<String, Boolean>> validateNumber(
            @PathVariable Long teamId,
            @RequestParam Short number,
            @RequestParam(defaultValue = "TEAM") String scope,
            @RequestParam(required = false) Long tournamentId
    ) {
        boolean available = playerService.validateNumber(teamId, number, scope, tournamentId);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /** 🔁 멤버를 선수로 편입
     * 팀에 이미 가입된 멤버(TeamMember)를 경기용 선수(Player)로 등록합니다.
     * 주로 팀원이 경기 출전을 시작할 때 호출됩니다.
     */
    @PostMapping("/from-member")
    @Operation(
            summary = "멤버를 선수로 편입",
            description = "팀에 이미 가입된 멤버를 선수(Player)로 등록합니다. (즉, User → Player 전환)"
    )
    public ResponseEntity<?> addFromMember(
            @PathVariable Long teamId,
            @RequestBody CreatePlayerRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        Long playerId = playerService.addFromMember(teamId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", playerId));
    }
}
