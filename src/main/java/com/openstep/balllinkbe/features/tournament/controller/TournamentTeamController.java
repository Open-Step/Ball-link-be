package com.openstep.balllinkbe.features.tournament.controller;

import com.openstep.balllinkbe.features.tournament.dto.response.PlayerCareerRecordResponse;
import com.openstep.balllinkbe.features.tournament.dto.response.TeamRecordDto;
import com.openstep.balllinkbe.features.tournament.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team/{teamId}")
@Tag(name = "team-records")
public class TournamentTeamController {

    private final TournamentService tournamentService;

    /** 팀 통산기록(시즌/전체)
     * TODO : wins, losses 표시할만한 컬럼 필요
     **/
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

    /**
     * 선수 통산기록(팀단위)
     **/
    @GetMapping("/players/stats")
    @Operation(summary = "선수 통산기록(팀단위)", description = "팀 소속 선수들의 합계/평균/랭킹 (시즌 필터 가능)")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = PlayerCareerRecordResponse.class)),
            examples = @ExampleObject(name = "성공예시", value = """
                  {
                    "teamId": 101,
                    "season": "FULL",
                    "split": "BOTH",
                    "rankBy" : "pts",
                    "items" : [
                          {"rank":1,"playerId":1023,"playerName":"김도현","backNumber":7,"gameCount":8,"pts":124,"ast":28,"reb":52,"stl":9,"blk":3,"fg2":35,"fg3":12,"ft":18}
                          {"rank":2,"playerId":1045,"playerName":"이서준","backNumber":12,"gameCount":8,"pts":90,"ast":21,"reb":40,"stl":6,"blk":2,"fg2":28,"fg3":8,"ft":10},
                          ...
                      ],
                   "page":0,
                   "size":20,
                   "total":26
                  }
                """)
        )
    )
    public ResponseEntity<PlayerCareerRecordResponse> getTeamRecords(
            @PathVariable Long teamId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "rankBy", defaultValue = "pts") String rankBy
    ) {
        return ResponseEntity.ok(tournamentService.getPlayerCareerRecords(teamId, page, size, rankBy));
    }

    /**
     * 선수 경기별 기록
     **/
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

    /** 팀 대회목록 조회
     * TODO : wins, losses 표시할만한 컬럼 필요
     * TODO : GAME_TEAM_STAT 테이블에서 어떻게 fg2, fg3, ft를 표현할 것인지 확인 필요(컬럼 없음)
     **/
    @GetMapping("/tournaments/participations")
    @Operation(summary = "팀 대회목록 조회", description = "해당 팀이 참여한 대회 요약 목록")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = PlayerCareerRecordResponse.class)),
            examples = @ExampleObject(name = "성공예시", value = """
                {
                  "content": [
                    {
                      "tournamentId": 101,
                      "tournamentName": "대회이름1",
                      "gameCount": 8,
                      "wins": 0,
                      "losses": 0,
                      "pts": 240,
                      "ast": 58,
                      "reb": 120,
                      "stl": 20,
                      "blk": 10,
                      "fg2": 0,
                      "fg3": 0,
                      "ft": 0
                    },
                    {
                      "tournamentId": 112,
                      "tournamentName": "대회이름12",
                      "gameCount": 6,
                      "wins": 0,
                      "losses": 0,
                      "pts": 178,
                      "ast": 41,
                      "reb": 96,
                      "stl": 14,
                      "blk": 7,
                      "fg2": 0,
                      "fg3": 0,
                      "ft": 0
                    }
                  ],
                  "pageable": {
                    "sort": { "empty": true, "sorted": false, "unsorted": true },
                    "offset": 0,
                    "pageNumber": 0,
                    "pageSize": 10,
                    "paged": true,
                    "unpaged": false
                  },
                  "last": false,
                  "totalPages": 3,
                  "totalElements": 27,
                  "size": 10,
                  "number": 0,
                  "sort": { "empty": true, "sorted": false, "unsorted": true },
                  "numberOfElements": 2,
                  "first": true,
                  "empty": false
                }
                
                """)
        )
    )
    public ResponseEntity<Page<TeamRecordDto>> getTeamRecords(
            @PathVariable Long teamId,
            @RequestParam(name = "season") String season,
            @RequestParam(name = "status") String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "rankBy", defaultValue = "pts") String rankBy
    ) {
        return ResponseEntity.ok(tournamentService.getTeamRecords(teamId, season, status, page, size, rankBy));
    }
}
