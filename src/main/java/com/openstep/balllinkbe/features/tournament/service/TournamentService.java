package com.openstep.balllinkbe.features.tournament.service;

import com.openstep.balllinkbe.features.tournament.dto.response.ParticipationRecordResponse;
import com.openstep.balllinkbe.features.tournament.dto.response.PlayerCareerRecordResponse;
import com.openstep.balllinkbe.features.tournament.dto.response.PlayerRecordDto;
import com.openstep.balllinkbe.features.tournament.repository.GamePlayerStatRepository;
import com.openstep.balllinkbe.features.tournament.repository.GameRepository;
import com.openstep.balllinkbe.features.tournament.repository.GameTeamStatsRepository;
import com.openstep.balllinkbe.features.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final GameRepository gameRepository;
    private final GameTeamStatsRepository gameTeamStatsRepository;
    private final GamePlayerStatRepository gamePlayerStatRepository;

    /*  특정 대회에서 해당 팀의 누적/경기당 기록 조회 */
    public ParticipationRecordResponse getParticipationRecord(String tournamentsId, String teamId, String aggregate) {
        //{ "tournamentId":31,"teamId":17,"games":8,"wins":6,"losses":2,"totals":{...},"perGame":{...} }


        //1. 특정 대회 조회 (tournamentsId -> Tournament)
        //2. 대회에 나온 특정 팀 기록 조회 ( tournamentsId + teamId -> 팀 기록??이??어디??

        //조합
        return ParticipationRecordResponse.builder().build();
    }

    /*  팀 대회목록 조회  */
    //{ "page":0,
    // "size":20,
    // "total":5,
    // "items":[ {
    //      "gameId":101,      //Game -> id
    //      "date":"2025-05-01",//Game -> scheduled_at
    //      "opponent":"YBC", //Game -> opponent_name
    //      "pts":17,        //Game_team_stats -> pts
    //      "reb":4,         // // Game_team_stats -> reb
    //      "ast":3,         // Game_team_stats -> ast
    //      "stl":2,         // Game_team_stats -> stl
    //      "blk":0,         // Game_team_stats -> blk
    //      "fg2":{...},
    //      "fg3":{...},
    //      "ft":{...}
    //    } ]
    // }
    public Object getTeamRecords(String teamId, String season, String status, int page, int size, String rankBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(rankBy).ascending());


    }


    //{ "teamId":17,
    // "season":"ALL",
    // "split":"TOTAL",
    // "rankBy":"pts",
    // "items":[ {
    //      "rank":1,
    //      "playerId":99,
    //      "playerName":"홍길동",
    //      "backNumber":23,
    //      "games":34,
    //      "totals":{...}
    //  } ],
    // "page":0,
    // "size":50,
    // "total":27 }
    public PlayerCareerRecordResponse getPlayerCareerRecords(Long teamId, int page, int size, String rankBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(rankBy).ascending());
        Page<PlayerRecordDto> records = gamePlayerStatRepository.findPlayerRecordsByTeam(teamId,rankBy,pageable);

        int base = records.getNumber() * records.getSize();

        List<PlayerRecordDto> items = new ArrayList<>(records.getContent().size());
        for(int i = 0; i < records.getContent().size(); i++){
            PlayerRecordDto playerRecordDto = records.getContent().get(i);
            playerRecordDto.setRank(base + i + 1);
            items.add(playerRecordDto);
        }

        return PlayerCareerRecordResponse.builder()
                .teamId(teamId)
                .season("FULL")
                .split("BOTH")
                .rankBy(rankBy)
                .items(items)
                .page(records.getNumber())
                .size(records.getSize())
                .total(records.getTotalElements())
                .build();
    }
}
