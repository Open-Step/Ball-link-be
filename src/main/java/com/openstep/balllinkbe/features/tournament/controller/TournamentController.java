package com.openstep.balllinkbe.features.tournament.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.tournament.dto.request.CreateTournamentRequest;
import com.openstep.balllinkbe.features.tournament.dto.request.UpdateTournamentRequest;
import com.openstep.balllinkbe.features.tournament.dto.response.TournamentResponse;
import com.openstep.balllinkbe.features.tournament.service.TournamentService;
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
@RequestMapping("/api/v1/tournaments")
@RequiredArgsConstructor
@Tag(name = "tournament-controller", description = "대회 관리 API")
public class TournamentController {

    private final TournamentService tournamentService;

    @PostMapping
    @Operation(summary = "대회 생성", description = "관리자만 대회를 생성할 수 있습니다.")
    public ResponseEntity<TournamentResponse> createTournament(
            @RequestBody CreateTournamentRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        if (!currentUser.isAdmin()) throw new CustomException(ErrorCode.FORBIDDEN);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentService.createTournament(req, currentUser));
    }

    @PatchMapping("/{tournamentId}")
    @Operation(summary = "대회 수정", description = "관리자만 대회 정보를 수정할 수 있습니다.")
    public ResponseEntity<TournamentResponse> updateTournament(
            @PathVariable Long tournamentId,
            @RequestBody UpdateTournamentRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        if (!currentUser.isAdmin()) throw new CustomException(ErrorCode.FORBIDDEN);
        return ResponseEntity.ok(tournamentService.updateTournament(tournamentId, req, currentUser));
    }

    @DeleteMapping("/{tournamentId}")
    @Operation(summary = "대회 삭제", description = "관리자만 대회를 삭제할 수 있습니다.")
    public ResponseEntity<Void> deleteTournament(
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal User currentUser
    ) {
        if (!currentUser.isAdmin()) throw new CustomException(ErrorCode.FORBIDDEN);
        tournamentService.deleteTournament(tournamentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "대회 목록 조회", description = "모든 대회를 조회합니다.")
    public ResponseEntity<List<TournamentResponse>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }


    @GetMapping("/{tournamentId}")
    @Operation(summary = "대회 상세 조회", description = "특정 대회 상세정보를 조회합니다.")
    public ResponseEntity<TournamentResponse> getTournament(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(tournamentService.getTournament(tournamentId));
    }
}
