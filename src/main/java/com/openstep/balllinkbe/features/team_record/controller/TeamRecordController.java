package com.openstep.balllinkbe.features.team_record.controller;

import com.openstep.balllinkbe.features.team_record.dto.response.*;
import com.openstep.balllinkbe.features.team_record.service.TeamRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "team-record-controller", description = "팀/선수 경기기록 및 통계 API")
public class TeamRecordController {

    private final TeamRecordService teamRecordService;

    /** 1) 팀 대회참가기록 */
    @GetMapping("/tournaments/{tournamentId}/teams/{teamId}/record")
    @Operation(summary = "팀 대회참가기록 조회", description = "특정 대회에서 해당 팀의 누적/경기당 기록을 조회합니다.")
    public ResponseEntity<TeamTournamentRecordResponse> getTeamTournamentRecord(
            @PathVariable Long tournamentId,
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "BOTH") String aggregate
    ) {
        return ResponseEntity.ok(teamRecordService.getTournamentRecord(tournamentId, teamId, aggregate));
    }

    /** 2) 팀 통산기록 (시즌/전체) */
    @GetMapping("/teams/{teamId}/records")
    @Operation(summary = "팀 통산기록(시즌/전체)", description = "팀 전체 혹은 특정 시즌의 누적 및 경기당 통계를 조회합니다.")
    public ResponseEntity<TeamSeasonRecordResponse> getTeamSeasonRecord(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "ALL") String season,
            @RequestParam(defaultValue = "BOTH") String split
    ) {
        return ResponseEntity.ok(teamRecordService.getSeasonRecord(teamId, season, split));
    }

    /** 3) 선수 통산기록 (팀단위, 랭킹) */
    @GetMapping("/teams/{teamId}/players/stats")
    @Operation(summary = "선수 통산기록(팀단위)", description = "팀 소속 선수들의 합계/평균/랭킹을 조회합니다.")
    public ResponseEntity<PlayerStatsResponse> getPlayerStats(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "ALL") String season,
            @RequestParam(defaultValue = "pts") String rankBy
    ) {
        return ResponseEntity.ok(teamRecordService.getPlayerStats(teamId, season, rankBy));
    }

    /** 4) 선수 경기별 기록 (페이징) */
    @GetMapping("/teams/{teamId}/players/{playerId}/games")
    @Operation(summary = "선수 경기별 기록", description = "특정 선수의 경기 단위 기록을 조회합니다.")
    public ResponseEntity<Page<PlayerGameRecordResponse.Item>> getPlayerGameRecords(
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @RequestParam(defaultValue = "ALL") String season,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date,desc") String sort
    ) {
        return ResponseEntity.ok(teamRecordService.getPlayerGameRecords(teamId, playerId, season, page, size, sort));
    }

    /** 5) 팀 대회목록 (참여 요약) */
    @GetMapping("/teams/{teamId}/tournaments/participations")
    @Operation(summary = "팀 대회목록 조회", description = "팀이 참가한 대회 목록과 요약 통계를 조회합니다.")
    public ResponseEntity<TournamentSummaryResponse> getTeamTournaments(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "ALL") String season
    ) {
        return ResponseEntity.ok(teamRecordService.getTeamTournaments(teamId, season));
    }

    /** 6) 대회 내 팀 경기목록 (페이징) */
    @GetMapping("/teams/{teamId}/tournaments/{tournamentId}/games")
    @Operation(summary = "대회 경기목록(팀 관점)", description = "특정 대회에서 해당 팀의 경기 목록을 조회합니다.")
    public ResponseEntity<Page<TournamentGameListResponse.Item>> getTournamentGames(
            @PathVariable Long teamId,
            @PathVariable Long tournamentId,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date,desc") String sort
    ) {
        return ResponseEntity.ok(teamRecordService.getTournamentGames(teamId, tournamentId, status, page, size, sort));
    }

    /** 7) 단일 경기 박스스코어 */
    @GetMapping("/games/{gameId}/boxscore")
    @Operation(summary = "경기 박스스코어", description = "한 경기의 팀 합계 및 선수별 스탯을 조회합니다.")
    public ResponseEntity<GameBoxscoreResponse> getGameBoxscore(@PathVariable Long gameId) {
        return ResponseEntity.ok(teamRecordService.getGameBoxscore(gameId));
    }
}
