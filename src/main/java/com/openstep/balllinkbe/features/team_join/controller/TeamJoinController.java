package com.openstep.balllinkbe.features.team_join.controller;

import com.openstep.balllinkbe.domain.team.Invite;
import com.openstep.balllinkbe.domain.team.JoinRequest;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_join.dto.request.JoinRequestDto;
import com.openstep.balllinkbe.features.team_join.dto.request.RejectDto;
import com.openstep.balllinkbe.features.team_join.dto.response.JoinAcceptResponse;
import com.openstep.balllinkbe.features.team_join.dto.response.JoinRequestResponse;
import com.openstep.balllinkbe.features.team_join.service.InviteService;
import com.openstep.balllinkbe.features.team_join.service.JoinRequestService;
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
@RequestMapping("/api/v1/join")
@RequiredArgsConstructor
@Tag(name = "team-join-controller", description = "팀 초대/가입신청 API")
public class TeamJoinController {

    private final InviteService inviteService;
    private final JoinRequestService joinRequestService;

    /** 초대 생성 */
    @PostMapping("/teams/{teamId}/invites")
    @Operation(summary = "초대 링크 생성", description = "팀장/매니저가 팀 초대 링크를 생성합니다. 생성된 링크는 공유 버튼을 통해 복사하여 전달할 수 있습니다.")
    public ResponseEntity<Map<String, String>> createInvite(@PathVariable Long teamId,
                                                            @AuthenticationPrincipal User currentUser) {
        Invite invite = inviteService.createInvite(teamId, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("code", invite.getCode()));
    }

    /** 초대 검증 */
    @GetMapping("/invites/{code}")
    @Operation(summary = "초대 링크 검증", description = "사용자가 초대 링크로 접근했을 때, 해당 링크의 유효성 및 팀 정보를 검증합니다.")
    public ResponseEntity<Invite> validateInvite(@PathVariable String code) {
        return ResponseEntity.ok(inviteService.validateInvite(code));
    }

    /** 초대 목록 조회 */
    @GetMapping("/teams/{teamId}/invites")
    @Operation(summary = "초대 목록 조회", description = "팀장/매니저가 생성한 초대 링크들의 목록을 조회합니다. 활성/회수된 링크 모두 확인 가능합니다.")
    public ResponseEntity<List<Invite>> listInvites(@PathVariable Long teamId,
                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(inviteService.listInvites(teamId, currentUser.getId()));
    }

    /** 초대 회수 */
    @DeleteMapping("/invites/{code}")
    @Operation(summary = "초대 링크 회수", description = "팀장/매니저가 초대 링크를 무효화(status=REVOKED) 합니다.")
    public ResponseEntity<Void> revokeInvite(@PathVariable String code,
                                             @RequestParam Long teamId, // 팀 구분 필요
                                             @AuthenticationPrincipal User currentUser) {
        inviteService.revokeInvite(code, teamId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /** 가입 신청 */
    @PostMapping("/requests")
    @Operation(summary = "가입 신청", description = "사용자가 팀 가입을 신청합니다. 공개팀은 초대코드 없이 신청 가능, 비공개팀은 초대코드 필수입니다.")
    public ResponseEntity<Map<String, Long>> apply(@RequestBody JoinRequestDto dto,
                                                   @AuthenticationPrincipal User currentUser) {
        JoinRequest req = joinRequestService.apply(
                dto.getTeamId(),
                currentUser.getId(),
                dto.getPosition(),
                dto.getLocation(),
                dto.getBio(),
                dto.getInviteCode()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", req.getId()));
    }

    /** 신청 목록 조회 */
    @GetMapping("/teams/{teamId}/requests")
    @Operation(summary = "가입 신청 목록 조회", description = "팀장/매니저가 특정 팀의 가입 신청 내역을 조회합니다.")
    public ResponseEntity<List<JoinRequestResponse>> listRequests(
            @PathVariable Long teamId,
            @RequestParam JoinRequest.Status status,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(joinRequestService.listRequests(teamId, currentUser.getId(), status));
    }


    /** 신청 수락 */
    @PostMapping("/requests/{reqId}/accept")
    @Operation(
            summary = "가입 신청 수락",
            description = "팀장/매니저가 가입 신청을 수락하면 팀 멤버 및 선수명단에 자동 등록됩니다. 응답으로 userId, playerId, memberId가 반환됩니다."
    )
    public ResponseEntity<JoinAcceptResponse> accept(
            @PathVariable Long reqId,
            @RequestParam Long teamId,
            @AuthenticationPrincipal User currentUser
    ) {
        JoinAcceptResponse response = joinRequestService.accept(reqId, teamId, currentUser.getId());
        return ResponseEntity.ok(response);
    }


    /** 신청 거절 */
    @PostMapping("/requests/{reqId}/reject")
    @Operation(summary = "가입 신청 거절", description = "팀장/매니저가 가입 신청을 거절합니다. 거절 사유는 rejectReason 컬럼에 기록됩니다.")
    public ResponseEntity<Map<String, Boolean>> reject(@PathVariable Long reqId,
                                                       @RequestParam Long teamId,
                                                       @AuthenticationPrincipal User currentUser,
                                                       @RequestBody RejectDto dto) {
        joinRequestService.reject(reqId, teamId, currentUser.getId(), dto.getReason());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
