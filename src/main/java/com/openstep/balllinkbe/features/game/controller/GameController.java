package com.openstep.balllinkbe.features.game.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.game.dto.request.CreateTournamentGameRequest;
import com.openstep.balllinkbe.features.game.dto.request.UpdateGameRequest;
import com.openstep.balllinkbe.features.game.dto.response.GameCreatedResponse;
import com.openstep.balllinkbe.features.game.service.GameService;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "game-controller", description = "경기 생성/수정 API")
public class GameController {

    private final GameService gameService;

    /** 대회 경기 생성 (관리자만 가능) */
    @PostMapping("/tournaments/{tournamentId}/games")
    @Operation(summary = "대회 경기 생성", description = "관리자만 대회 경기를 생성할 수 있습니다.")
    public ResponseEntity<GameCreatedResponse> createTournamentGame(
            @PathVariable Long tournamentId,
            @RequestBody CreateTournamentGameRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        if (!currentUser.isAdmin()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        Long id = gameService.createTournamentGame(tournamentId, req, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GameCreatedResponse(id));
    }

    @PatchMapping("/games/{gameId}")
    @Operation(summary = "경기 기본정보 수정", description = "일정/장소/라운드/브래킷 순서 등을 수정합니다.")
    public ResponseEntity<?> updateGame(
            @PathVariable Long gameId,
            @RequestBody UpdateGameRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        gameService.updateGame(gameId, req, currentUser);
        return ResponseEntity.ok().build();
    }
}
