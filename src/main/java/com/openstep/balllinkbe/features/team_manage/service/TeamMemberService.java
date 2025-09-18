package com.openstep.balllinkbe.features.team_manage.service;

import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdateRoleRequest;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamMemberResponse;
import com.openstep.balllinkbe.features.team_manage.repository.TeamMemberRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    /** 팀 멤버 목록 조회 */
    public Page<TeamMemberResponse> getMembers(Long teamId, int page, int size, User currentUser) {
        var members = teamMemberRepository.findByTeamIdAndLeftAtIsNull(teamId);
        List<TeamMemberResponse> responses = members.stream()
                .map(TeamMemberResponse::new)
                .toList();
        return new PageImpl<>(responses, PageRequest.of(page, size), responses.size());
    }

    /** 멤버 권한 변경 (팀장만 가능, 매니저 2명 제한) */
    public void updateRole(Long teamId, Long userId, UpdateRoleRequest request, User currentUser) {
        // 현재 사용자가 팀장인지 검증
        var currentLeader = teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if (currentLeader.getRole() != TeamMember.Role.LEADER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        var member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        TeamMember.Role newRole = request.toEnum();

        // 매니저 인원 제한 검증 (2명까지만 가능)
        if (newRole == TeamMember.Role.MANAGER) {
            long managerCount = teamMemberRepository.countByTeamIdAndRoleAndLeftAtIsNull(teamId, TeamMember.Role.MANAGER);
            if (managerCount >= 2) {
                throw new CustomException(ErrorCode.INVALID_REQUEST); // 필요하다면 ErrorCode.MANAGER_LIMIT_EXCEEDED 추가 가능
            }
        }

        member.setRole(newRole);
        teamMemberRepository.save(member);
    }

    /** 팀장 위임 */
    public void transferOwnership(Long teamId, Long toUserId, User currentUser) {
        // 현재 사용자가 팀장인지 검증
        var currentLeader = teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if (currentLeader.getRole() != TeamMember.Role.LEADER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        var newLeader = teamMemberRepository.findByTeamIdAndUserId(teamId, toUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 역할 변경
        currentLeader.setRole(TeamMember.Role.PLAYER);
        newLeader.setRole(TeamMember.Role.LEADER);

        teamMemberRepository.save(currentLeader);
        teamMemberRepository.save(newLeader);
    }
}
