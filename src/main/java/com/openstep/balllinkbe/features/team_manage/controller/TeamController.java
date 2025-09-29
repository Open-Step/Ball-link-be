package com.openstep.balllinkbe.features.team_manage.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.CreateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamDetailResponse;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamResponse;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamSummaryResponse;
import com.openstep.balllinkbe.features.team_manage.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "team-controller", description = "팀 관리 API (생성, 조회, 수정, 삭제, 탈퇴)")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/me")
    @Operation(summary = "내가 가입한 팀 목록 조회")
    public ResponseEntity<List<TeamSummaryResponse>> getMyTeams(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(teamService.getMyTeams(currentUser));
    }

    @PostMapping
    @Operation(summary = "팀 생성")
    public ResponseEntity<?> createTeam(@Valid @RequestBody CreateTeamRequest request,
                                        @AuthenticationPrincipal User currentUser) {
        Long teamId = teamService.createTeam(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", teamId));
    }

    @PatchMapping("/{teamId}")
    @Operation(summary = "팀 수정 (팀장만 가능)")
    public ResponseEntity<TeamResponse> updateTeam(@PathVariable Long teamId,
                                                   @RequestBody UpdateTeamRequest request,
                                                   @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(teamService.updateTeam(teamId, request, currentUser));
    }

    @PatchMapping(value = "/{teamId}/emblem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "팀 엠블럼 변경 (팀장만 가능)")
    public ResponseEntity<Map<String, String>> updateTeamEmblem(@PathVariable Long teamId,
                                                                @RequestPart("file") MultipartFile file,
                                                                @AuthenticationPrincipal User currentUser) {
        String url = teamService.updateTeamEmblem(teamId, file, currentUser);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping
    @Operation(summary = "팀 목록 조회 (공개팀만)")
    public ResponseEntity<Page<TeamSummaryResponse>> getTeams(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size,
                                                              @RequestParam(defaultValue = "name,asc") String sort,
                                                              @RequestParam(required = false) String q) {
        return ResponseEntity.ok(teamService.getTeams(page, size, sort, q));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "팀 상세 조회")
    public ResponseEntity<TeamDetailResponse> getTeamDetail(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeamDetail(teamId));
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "팀 해산 (soft delete, 팀장만 가능)")
    public ResponseEntity<?> deleteTeam(@PathVariable Long teamId,
                                        @AuthenticationPrincipal User currentUser) {
        teamService.deleteTeam(teamId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{teamId}/members/me")
    @Operation(summary = "팀 탈퇴 (팀장일 경우 위임 필요)")
    public ResponseEntity<?> leaveTeam(@PathVariable Long teamId,
                                       @RequestParam(required = false) Long transferToUserId,
                                       @AuthenticationPrincipal User currentUser) {
        teamService.leaveTeam(teamId, currentUser, transferToUserId);
        return ResponseEntity.noContent().build();
    }
}
