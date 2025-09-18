package com.openstep.balllinkbe.features.team_manage.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.CreateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "team-controller", description = "팀 관리 API (생성, 조회, 수정, 삭제)")
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "팀 생성", description = "신규 팀을 생성합니다. 팀명, 약칭, 창단년도, 지역 등의 정보를 입력해야 합니다.")
    public ResponseEntity<?> createTeam(@RequestBody CreateTeamRequest request,
                                        @AuthenticationPrincipal User currentUser) {
        Long teamId = teamService.createTeam(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", teamId));
    }
}
