package com.openstep.balllinkbe.features.team_manage.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdateRoleRequest;
import com.openstep.balllinkbe.features.team_manage.dto.request.TransferOwnershipRequest;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamMemberResponse;
import com.openstep.balllinkbe.features.team_manage.service.TeamMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/teams/members")
@RequiredArgsConstructor
@Tag(name = "team-member-controller", description = "팀 멤버 관리 API (조회, 권한 변경, 팀장 위임)")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    /** 멤버 목록 조회 */
    @GetMapping("/{teamId}")
    @Operation(summary = "멤버 목록 조회", description = "팀 멤버와 역할 정보를 조회합니다. (페이징)")
    public ResponseEntity<Page<TeamMemberResponse>> getMembers(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(teamMemberService.getMembers(teamId, page, size, currentUser));
    }

    /** 권한 변경 */
    @PatchMapping("/{teamId}/{userId}/role")
    @Operation(summary = "권한 변경", description = "팀장이 멤버 권한을 변경합니다. (최대 2명까지 매니저 가능)")
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
}
