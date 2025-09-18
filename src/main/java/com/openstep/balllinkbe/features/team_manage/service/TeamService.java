package com.openstep.balllinkbe.features.team_manage.service;

import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.CreateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamResponse;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamSummaryResponse;
import com.openstep.balllinkbe.features.team_manage.repository.TeamMemberRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import com.openstep.balllinkbe.features.user.repository.UserRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    /** 팀 생성 */
    @Transactional
    public Long createTeam(CreateTeamRequest dto, User currentUser) {
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
        team.setOwnerUser(owner);
        team.setCreatedAt(LocalDateTime.now());

        Team saved = teamRepository.save(team);

        TeamMember member = new TeamMember();
        member.setTeam(saved);
        member.setUser(owner);
        member.setRole(TeamMember.Role.LEADER);
        teamMemberRepository.save(member);

        return saved.getId();
    }

    /** 팀 수정 */
    @Transactional
    public Team updateTeam(Long teamId, UpdateTeamRequest dto, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

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

    /** 팀 목록 조회 */
    public Page<TeamSummaryResponse> getTeams(int page, int size, String sort, String q) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort.split(",")));
        Page<Team> teams;

        if (q != null && !q.isBlank()) {
            teams = teamRepository.findByNameContainingAndIsPublicTrue(q, pageable);
        } else {
            teams = teamRepository.findByIsPublicTrue(pageable);
        }

        // 여기서 TeamSummaryResponse 생성자 매핑
        return teams.map(TeamSummaryResponse::new);
    }

    /** 팀 상세 조회 */
    public TeamResponse getTeamDetail(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        return new TeamResponse(team);
    }

    /** 팀 삭제 (soft delete) */
    @Transactional
    public void deleteTeam(Long teamId, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.getOwnerUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        team.setDeletedAt(LocalDateTime.now());
        teamRepository.save(team);
    }

    /** 팀 탈퇴 */
    @Transactional
    public void leaveTeam(Long teamId, User currentUser, Long transferToUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        TeamMember member = teamMemberRepository.findByTeamAndUser(team, currentUser)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1) Owner가 아닌 경우 → 그냥 탈퇴
        if (!team.getOwnerUser().getId().equals(currentUser.getId())) {
            teamMemberRepository.delete(member);
            return;
        }

        // 2) Owner인 경우
        if (transferToUserId != null) {
            // 위임할 유저 확인
            User newOwner = userRepository.findById(transferToUserId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            TeamMember newOwnerMember = teamMemberRepository.findByTeamAndUser(team, newOwner)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // 권한 위임
            team.setOwnerUser(newOwner);
            newOwnerMember.setRole(TeamMember.Role.LEADER);

            // 기존 owner는 PLAYER로 강등
            member.setRole(TeamMember.Role.PLAYER);

            teamRepository.save(team);
            teamMemberRepository.save(newOwnerMember);
            teamMemberRepository.save(member);

        } else {
            // 위임할 대상 없으면 팀 해산 처리
            team.setDeletedAt(LocalDateTime.now());
            teamRepository.save(team);
        }
    }

}
