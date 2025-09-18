package com.openstep.balllinkbe.features.team_manage.controller;

import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.CreateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamDetailResponse;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamResponse;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamSummaryResponse;
import com.openstep.balllinkbe.features.team_manage.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "team-controller", description = "팀 관리 API (생성, 조회, 수정, 삭제, 탈퇴)")
public class TeamController {
    private final TeamService teamService;

    /* 사용자가 가입한 팀 목록(사용자는 최대 3개의 팀에 들어갈 수 있음 */
    @GetMapping("/me")
    @Operation(summary = "내가 가입한 팀 목록 조회", description = "로그인 사용자가 가입한 팀 목록 (최대 3개) 조회")
    public ResponseEntity<List<TeamSummaryResponse>> getMyTeams(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(teamService.getMyTeams(currentUser));
    }

    /** 팀 생성 */
    @PostMapping
    @Operation(summary = "팀 생성", description = "신규 팀을 생성합니다. 팀명, 약칭, 창립일, 지역 등의 정보를 입력해야 합니다.")
    public ResponseEntity<?> createTeam(@Valid @RequestBody CreateTeamRequest request,
                                        @AuthenticationPrincipal User currentUser) {
        Long teamId = teamService.createTeam(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", teamId));
    }

    /** 팀 수정 */
    @PatchMapping("/{teamId}")
    @Operation(summary = "팀 수정",
            description = "기존 팀 정보를 수정합니다. 팀장만 가능.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 팀 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateTeamRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "팀 수정 예시",
                                            value = "{ \"description\": \"팀 설명 수정\", \"isPublic\": false, \"emblemUrl\": \"https://cdn.example.com/emblem.png\" }"
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<TeamResponse> updateTeam(@PathVariable Long teamId,
                                                   @RequestBody UpdateTeamRequest request,
                                                   @AuthenticationPrincipal User currentUser) {
        Team updatedTeam = teamService.updateTeam(teamId, request, currentUser);
        return ResponseEntity.ok(new TeamResponse(updatedTeam));
    }

    /** 팀 목록 조회 */
    @GetMapping
    @Operation(summary = "팀 목록 조회", description = "팀 목록을 페이지네이션 방식으로 조회합니다. 공개팀만 기본 조회.")
    public ResponseEntity<Page<TeamSummaryResponse>> getTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(teamService.getTeams(page, size, sort, q));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "팀 상세 조회", description = "팀의 상세 정보를 조회합니다.")
    public ResponseEntity<TeamDetailResponse> getTeamDetail(@PathVariable Long teamId) {
        TeamDetailResponse response = teamService.getTeamDetail(teamId);
        return ResponseEntity.ok(response);
    }


    /** 팀 해산(삭제) */
    @DeleteMapping("/{teamId}")
    @Operation(summary = "팀 해산", description = "팀을 삭제합니다. (soft delete, 팀장은 본인 팀만 가능)")
    public ResponseEntity<?> deleteTeam(@PathVariable Long teamId,
                                        @AuthenticationPrincipal User currentUser) {
        teamService.deleteTeam(teamId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /** 팀 탈퇴 (팀장일 경우 transferToUserId 필요) */
    @DeleteMapping("/{teamId}/members/me")
    @Operation(summary = "팀 탈퇴", description = "본인이 팀에서 탈퇴합니다. 팀장일 경우 팀 해산 또는 위임 필요.")
    public ResponseEntity<?> leaveTeam(
            @PathVariable Long teamId,
            @RequestParam(required = false) Long transferToUserId,
            @AuthenticationPrincipal User currentUser
    ) {
        teamService.leaveTeam(teamId, currentUser, transferToUserId);
        return ResponseEntity.noContent().build();
    }

}
