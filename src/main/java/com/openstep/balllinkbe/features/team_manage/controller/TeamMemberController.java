package com.openstep.balllinkbe.features.team_manage.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdateRoleRequest;
import com.openstep.balllinkbe.features.team_manage.dto.request.TransferOwnershipRequest;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamMemberResponse;
import com.openstep.balllinkbe.features.team_manage.service.TeamMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teams/members")
@RequiredArgsConstructor
@Tag(name = "team-member-controller", description = "팀 멤버 관리 API (조회, 권한 변경, 팀장 위임)")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    /** 멤버 목록 조회 */
    @GetMapping("/{teamId}")
    @Operation(summary = "멤버 목록 조회", description = "팀 멤버의 역할, 등번호, 포지션, 활동 지역 정보를 조회합니다.")
    public ResponseEntity<List<TeamMemberResponse>> getMembers(
            @PathVariable Long teamId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(teamMemberService.getMembers(teamId, currentUser));
    }


    /** 권한 변경 */
    @PatchMapping("/{teamId}/{userId}/role")
    @Operation(summary = "선수명단-수정", description = "팀장|매니저가 선수의 포지션|권한|등번호를 수정합니다.")
    public ResponseEntity<?> updateRole(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        teamMemberService.updateRole(teamId, userId, request, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 팀장 위임 */
    @PostMapping("/{teamId}/transfer-ownership")
    @Operation(summary = "팀장 위임", description = "팀장이 다른 멤버에게 팀장 권한을 위임합니다.")
    public ResponseEntity<?> transferOwnership(
            @PathVariable Long teamId,
            @RequestBody TransferOwnershipRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        teamMemberService.transferOwnership(teamId, request.getToUserId(), currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 팀원 강퇴 */
    @DeleteMapping("/{teamId}/members/{userId}")
    @Operation(
            summary = "팀원 강퇴",
            description = """
        팀장 또는 매니저가 특정 팀원을 강퇴합니다.
        ⚙ 처리 내용:
        - team_members 테이블의 left_at 값을 기록 (논리적 탈퇴)
        - 해당 유저의 players 엔티티가 존재하면 deleted_at, is_active=false로 비활성화
        - 팀장은 강퇴할 수 없습니다.
        """
    )
    public ResponseEntity<?> kickMember(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser
    ) {
        teamMemberService.kickMember(teamId, userId, currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
