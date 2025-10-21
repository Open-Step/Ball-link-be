package com.openstep.balllinkbe.features.game.controller;

import com.openstep.balllinkbe.features.game.dto.response.GameSummaryResponse;
import com.openstep.balllinkbe.features.game.service.GameQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tournaments/{tournamentId}/games")
@RequiredArgsConstructor
@Tag(name = "game-query-controller", description = "경기 조회 API")
public class GameQueryController {

    private final GameQueryService gameQueryService;

    @GetMapping
    @Operation(summary = "대회 경기 목록 조회", description = "대회별 경기 목록을 조회합니다.")
    public ResponseEntity<List<GameSummaryResponse>> getTournamentGames(
            @PathVariable Long tournamentId
    ) {
        return ResponseEntity.ok(gameQueryService.getTournamentGames(tournamentId));
    }
}
