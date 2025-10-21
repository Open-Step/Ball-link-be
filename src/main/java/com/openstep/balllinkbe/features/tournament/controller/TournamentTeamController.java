package com.openstep.balllinkbe.features.tournament.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.tournament.dto.request.AddTournamentTeamRequest;
import com.openstep.balllinkbe.features.tournament.dto.response.TournamentTeamResponse;
import com.openstep.balllinkbe.features.tournament.service.TournamentTeamService;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tournaments/{tournamentId}/teams")
@RequiredArgsConstructor
@Tag(name = "tournament-team-controller", description = "대회 참가팀 관리 API")
public class TournamentTeamController {

    private final TournamentTeamService tournamentTeamService;

    @PostMapping
    @Operation(summary = "대회 참가팀 등록", description = "관리자만 참가팀을 등록할 수 있습니다.")
    public ResponseEntity<TournamentTeamResponse> addTeam(
            @PathVariable Long tournamentId,
            @RequestBody AddTournamentTeamRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        if (!currentUser.isAdmin()) throw new CustomException(ErrorCode.FORBIDDEN);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentTeamService.addTeam(tournamentId, req));
    }

    @GetMapping
    @Operation(summary = "대회 참가팀 목록 조회", description = "대회에 등록된 팀 목록을 조회합니다.")
    public ResponseEntity<List<TournamentTeamResponse>> getTeams(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(tournamentTeamService.getTeams(tournamentId));
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "대회 참가팀 삭제", description = "관리자만 대회 참가팀을 삭제할 수 있습니다.")
    public ResponseEntity<Void> removeTeam(
            @PathVariable Long tournamentId,
            @PathVariable Long teamId,
            @AuthenticationPrincipal User currentUser
    ) {
        if (!currentUser.isAdmin()) throw new CustomException(ErrorCode.FORBIDDEN);
        tournamentTeamService.removeTeam(tournamentId, teamId);
        return ResponseEntity.noContent().build();
    }
}
