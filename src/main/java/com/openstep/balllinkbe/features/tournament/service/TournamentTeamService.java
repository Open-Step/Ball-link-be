package com.openstep.balllinkbe.features.tournament.service;

import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.tournament.Tournament;
import com.openstep.balllinkbe.domain.tournament.TournamentTeam;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import com.openstep.balllinkbe.features.tournament.dto.request.AddTournamentTeamRequest;
import com.openstep.balllinkbe.features.tournament.dto.response.TournamentTeamResponse;
import com.openstep.balllinkbe.features.tournament.repository.TournamentRepository;
import com.openstep.balllinkbe.features.tournament.repository.TournamentTeamRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TournamentTeamService {

    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final TeamRepository teamRepository;

    /** ✅ 대회 참가팀 등록 */
    public TournamentTeamResponse addTeam(Long tournamentId, AddTournamentTeamRequest req) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new CustomException(ErrorCode.TOURNAMENT_NOT_FOUND));

        Team team = teamRepository.findById(req.getTeamId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        if (tournamentTeamRepository.existsByTournamentIdAndTeamId(tournamentId, req.getTeamId())) {
            throw new CustomException(ErrorCode.DUPLICATE_TEAM_TAG);
        }

        TournamentTeam tt = TournamentTeam.builder()
                .tournament(tournament)
                .team(team)
                .seed(req.getSeed())
                .createdAt(LocalDateTime.now())
                .build();

        tournamentTeamRepository.save(tt);
        return new TournamentTeamResponse(tt);
    }

    /** ✅ 대회 참가팀 목록 조회 */
    @Transactional
    public List<TournamentTeamResponse> getTeams(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new CustomException(ErrorCode.TOURNAMENT_NOT_FOUND));

        List<TournamentTeam> list = tournamentTeamRepository.findByTournament(tournament);
        return list.stream().map(TournamentTeamResponse::new).toList();
    }

    /** ✅ 참가팀 삭제 */
    public void removeTeam(Long tournamentId, Long teamId) {
        TournamentTeam tt = tournamentTeamRepository.findByTournamentIdAndTeamId(tournamentId, teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        tournamentTeamRepository.delete(tt);
    }
}
