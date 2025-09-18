package com.openstep.balllinkbe.features.team_manage.service;

import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.CreateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.repository.TeamMemberRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import com.openstep.balllinkbe.features.user.repository.UserRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;   // 추가

    @Transactional
    public Long createTeam(CreateTeamRequest dto, User currentUser) {
        // DB에서 다시 조회 (영속성 보장)
        User owner = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Team team = new Team();
        team.setName(dto.getName());
        team.setShortName(dto.getShortName());
        team.setFoundedAt(dto.getFoundedAt());
        team.setRegion(dto.getRegion());
        team.setDescription(dto.getDescription());
        team.setEmblemUrl(dto.getEmblemUrl());
        team.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : true);
        team.setOwnerUser(owner);   // null 방지

        Team saved = teamRepository.save(team);

        // 팀 생성자 -> 팀멤버 LEADER 등록
        TeamMember member = new TeamMember();
        member.setTeam(saved);
        member.setUser(owner);
        member.setRole(TeamMember.Role.LEADER);
        teamMemberRepository.save(member);

        return saved.getId();
    }

    @Transactional
    public Team updateTeam(Long teamId, UpdateTeamRequest dto, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        // ownerUser가 null일 경우 대비
        if (team.getOwnerUser() == null ||
                !team.getOwnerUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        if (dto.getDescription() != null) team.setDescription(dto.getDescription());
        if (dto.getFoundedAt() != null) team.setFoundedAt(dto.getFoundedAt());
        if (dto.getIsPublic() != null) team.setIsPublic(dto.getIsPublic());
        if (dto.getEmblemUrl() != null) team.setEmblemUrl(dto.getEmblemUrl());

        team.setUpdatedAt(LocalDateTime.now());
        return teamRepository.save(team);
    }
}
