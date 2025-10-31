package com.openstep.balllinkbe.features.tournament.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.tournament.dto.request.AddEntryRequest;
import com.openstep.balllinkbe.features.tournament.dto.request.AddTournamentTeamRequest;
import com.openstep.balllinkbe.features.tournament.dto.response.EntryResponse;
import com.openstep.balllinkbe.features.tournament.service.TournamentEntryService;
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
@RequestMapping("/api/v1/games/{gameId}/entries")
@RequiredArgsConstructor
@Tag(name = "tournament-entry-controller", description = "경기 엔트리 관리 API")
public class TournamentEntryController {

    private final TournamentEntryService entryService;

    @GetMapping
    @Operation(summary = "경기 엔트리 조회", description = "양 팀의 엔트리(선수 목록)를 조회합니다.")
    public ResponseEntity<List<EntryResponse>> getEntries(@PathVariable Long gameId) {
        return ResponseEntity.ok(entryService.getEntries(gameId));
    }

    @PostMapping
    public ResponseEntity<Void> updateEntries(
            @PathVariable Long gameId,
            @RequestBody List<AddTournamentTeamRequest> reqList,
            @AuthenticationPrincipal User user
    ) {
        if (!user.isAdmin()) throw new CustomException(ErrorCode.FORBIDDEN);
        entryService.updateEntries(gameId, reqList); // 시그니처가 딱 맞음
        return ResponseEntity.ok().build();
    }
}
