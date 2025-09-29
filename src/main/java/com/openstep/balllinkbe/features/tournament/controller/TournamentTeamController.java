package com.openstep.balllinkbe.features.tournament.controller;

import com.openstep.balllinkbe.features.tournament.dto.response.PlayerCareerRecordResponse;
import com.openstep.balllinkbe.features.tournament.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team/{teamId}")
@Tag(name = "team-records")
public class TournamentTeamController {

    private final TournamentService  tournamentService;

    /** 팀 통산기록(시즌/전체) **/
    /** TODO : wins, losses 표시할만한 컬럼 필요 **/
    @GetMapping("/records")
    @Operation(summary = "팀 통산기록(시즌/전체)", description = "팀 전체 시즌/특정 시즌의 누적 및 경기당 통계")
    public ResponseEntity<?> getTeamRecords(
            @PathVariable Long teamId,
            @RequestParam(name = "season") String season, //ALL
            @RequestParam(name = "split") String split  //TOTAL|PER_GAME|BOTH
    ) {
        //{ "teamId":17,
        // "season":"ALL",
        // "split":"TOTAL",
        // "games":45,
        // "wins":28,
        // "losses":17,
        // "totals":{...},
        // "perGame":{...} }
        return ResponseEntity.ok(null);
    }

    /**선수 통산기록(팀단위) **/
    @GetMapping("/players/stats")
    @Operation(summary = "선수 통산기록(팀단위)", description = "팀 소속 선수들의 합계/평균/랭킹 (시즌 필터 가능)")
    public ResponseEntity<PlayerCareerRecordResponse> getTeamRecords(
            @PathVariable Long teamId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "rankBy", defaultValue = "pts") String rankBy
    ) {
        return ResponseEntity.ok(tournamentService.getPlayerCareerRecords(teamId, page, size, rankBy));
    }

    /** 선수 경기별 기록 **/
    @GetMapping("/players/{playerId}/games")
    @Operation(summary = "선수 경기별 기록", description = "특정 선수의 시즌별/대회별 경기 단위 기록 조회")
    public ResponseEntity<?> getTeamRecords(
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "rankBy", defaultValue = "pts") String rankBy
    ) {
        //{ "page":0,
        // "size":20,
        // "total":1,
        // "items":[{
        //      "tournamentId":31,
        //      "tournamentName":"2025 봄 리그",
        //      "season":"2025",
        //      "status":"FINISHED",
        //      "startDate":"2025-03-01",
        //      "endDate":"2025-05-20",
        //      "summary":{
        //          "games":8,
        //          "wins":6,
        //          "losses":2,
        //          "pts":512 }
        //       }]
        // }
        return ResponseEntity.ok(null);
    }

    /** 팀 대회목록 조회 **/
    /** TODO : wins, losses 표시할만한 컬럼 필요 **/
    @GetMapping("/tournaments/participations")
    @Operation(summary = "팀 대회목록 조회", description = "해당 팀이 참여한 대회 요약 목록")
    public ResponseEntity<?> getTeamRecords(
            @PathVariable Long teamId,
            @RequestParam(name = "season") String season,
            @RequestParam(name = "status") String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "rankBy", defaultValue = "pts") String rankBy
    ) {
        //{ "page":0,
        // "size":20,
        // "total":5,
        // "items":[ {
        //      "gameId":101,      //Game -> id
        //      "date":"2025-05-01",//Game -> scheduled_at
        //      "opponent":"YBC", //Game -> opponent_name
        //      "pts":17,        //Game -> home_team_id -> Game_team_stats -> pts
        //      "reb":4,         // // home_team_id -> Game_team_stats -> reb
        //      "ast":3,         // home_team_id -> Game_team_stats -> ast
        //      "stl":2,         // home_team_id -> Game_team_stats -> stl
        //      "blk":0,         // home_team_id -> Game_team_stats -> blk
        //      "fg2":{...},
        //      "fg3":{...},
        //      "ft":{...}
        //    } ]
        // }
        return ResponseEntity.ok(tournamentService.getTeamRecords(teamId, season, status, page, size, rankBy));
    }
}
