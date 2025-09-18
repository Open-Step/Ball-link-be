package com.openstep.balllinkbe.features.team_manage.controller;

import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.CreateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamResponse;
import com.openstep.balllinkbe.features.team_manage.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "team-controller", description = "팀 관리 API (생성, 조회, 수정, 삭제)")
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "팀 생성", description = "신규 팀을 생성합니다. 팀명, 약칭, 창단년도, 지역 등의 정보를 입력해야 합니다.")
    public ResponseEntity<?> createTeam(@Valid @RequestBody CreateTeamRequest request,
                                        @AuthenticationPrincipal User currentUser) {
        Long teamId = teamService.createTeam(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", teamId));
    }

    @PatchMapping("/{teamId}")
    @Operation(summary = "팀 수정",
            description = "기존 팀 정보를 수정합니다.",
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

}
