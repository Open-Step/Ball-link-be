package com.openstep.balllinkbe.features.tournament.service;

import com.openstep.balllinkbe.features.tournament.dto.response.ParticipationRecordResponse;
import com.openstep.balllinkbe.features.tournament.dto.response.PlayerCareerRecordResponse;
import com.openstep.balllinkbe.features.tournament.dto.response.PlayerRecordDto;
import com.openstep.balllinkbe.features.tournament.dto.response.TeamRecordDto;
import com.openstep.balllinkbe.features.tournament.repository.GamePlayerStatRepository;
import com.openstep.balllinkbe.features.tournament.repository.GameTeamStatRepository;
import com.openstep.balllinkbe.features.tournament.repository.TournamentRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final GameTeamStatRepository gameTeamStatRepository;
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
    /** fg2, fg3, ft를 팀단위, 토너먼트단위로 구하려면 game_player_stat를 다시 조인해야하는데
     * 그보다는 기존에 저장되어있는 game_team_states에 fg2, fg3, ft 추가하는게 낫지않을까 싶음.
     * 추가로 season, status는 깡통 값이라서 사용안했는데, 사용시에는 쿼리문에 where문 더해주면 됨 . **/
    public Page<TeamRecordDto> getTeamRecords(Long teamId, String season, String status, int page, int size, String rankBy) {
        Pageable pageable = PageRequest.of(page, size);
        //해당 토너먼트에서 있었던 게임들 찾기 Map<토너먼트아이디, List<게임아이디들>>
        // 토너먼트아이디1 -> { 게임아이디1, 게임아이디2, 게임아이디2 ... }
        // 토너먼트아이디2 -> { 게임아이디10, 게임아이디11, 게임아이디12 ...}

        // 각 토너먼트 안에서 경기 수, 총 득점, 어시, 리바운드, 스틸, 블록, 2점, 3점, 자유투 값 더해서 가져오기
        // 토너먼트아이디1 -> { 경기수, 총 득점, 어시, 리바운드, 스틸, 블록, 2점, 3점, 자유투}
        // 토너먼트아이디1 -> { 경기수, 총 득점, 어시, 리바운드, 스틸, 블록, 2점, 3점, 자유투}

        //  한방쿼리로 한다면
        // game + game_team_stat -> teamId로 필터링 후 tournament로 그룹핑
        //페이로드 조립
        return gameTeamStatRepository.findTeamRecordsByTournamentId(teamId, pageable);
    }

    public PlayerCareerRecordResponse getPlayerCareerRecords(Long teamId, int page, int size, String rankBy) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PlayerRecordDto> records = gamePlayerStatRepository.findPlayerRecordsByTeam(teamId,rankBy,pageable);

        if(records.getTotalElements() == 0){
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        int base = records.getNumber() * records.getSize();

        List<PlayerRecordDto> items = new ArrayList<>(records.getContent().size());
        for(int i = 0; i < records.getContent().size(); i++){
            PlayerRecordDto playerRecordDto = records.getContent().get(i);
            playerRecordDto.setRank(base + i + 1);
            items.add(playerRecordDto);
        }

        return PlayerCareerRecordResponse.builder()
                .teamId(teamId)
                .season("FULL") //어디서 가져와야하는건지
                .split("BOTH")  //몰라서 기본값으로 일단 집어넣음 << PRE-GAME|TOTAL|BOTH 가 각각어떤 옵션인지
                .rankBy(rankBy)
                .items(items)
                .page(records.getNumber())
                .size(records.getSize())
                .total(records.getTotalElements())
                .build();
    }
}
