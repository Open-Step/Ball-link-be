package com.openstep.balllinkbe.features.tournament.controller;

import com.openstep.balllinkbe.features.tournament.dto.response.ParticipationRecordResponse;
import com.openstep.balllinkbe.features.tournament.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tournaments")
@Tag(name = "team-records")
public class TournamentController {

    private final TournamentService tournamentService;

    /** 팀 대회 참가기록 조회 **/
    @GetMapping("/{tournamentsId}/teams/{teamId}")
    @Operation(summary = "팀 대회 참가기록 조회", description = "특정 대회에서 해당 팀의 누적/경기당 기록을 조회합니다.")
    public ResponseEntity<ParticipationRecordResponse> getParticipationRecord(
            @PathVariable String tournamentsId,
            @PathVariable String teamId,
            @RequestParam(name = "aggregate") String aggregate //TOTAL|PER_GAME|BOTH
    ) {
        //{ "tournamentId":31,
        // "teamId":17,
        // "games":8,
        // "wins":6,
        // "losses":2,
        // "totals":{...},
        // "perGame":{...}
        // }
        return ResponseEntity.ok(tournamentService.getParticipationRecord(tournamentsId,teamId,aggregate));
    }

}
