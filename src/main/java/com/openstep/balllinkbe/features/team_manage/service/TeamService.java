package com.openstep.balllinkbe.features.team_manage.service;

import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.CreateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.repository.TeamMemberRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public Long createTeam(CreateTeamRequest dto, User currentUser) {
        Team team = new Team();
        team.setName(dto.getName());
        team.setShortName(dto.getShortName());
        team.setFoundedYear(dto.getFoundedYear());
        team.setRegion(dto.getRegion());
        team.setDescription(dto.getDescription());
        team.setColorPrimary(dto.getColorPrimary());
        team.setOwner(currentUser);

        Team saved = teamRepository.save(team);

        // 팀 생성자 -> 팀멤버 LEADER 등록
        TeamMember member = new TeamMember();
        member.setTeam(saved);
        member.setUser(currentUser);
        member.setRole(TeamMember.Role.LEADER);
        teamMemberRepository.save(member);

        return saved.getId();
    }
}
