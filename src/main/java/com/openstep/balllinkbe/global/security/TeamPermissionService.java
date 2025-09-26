package com.openstep.balllinkbe.global.security;

import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.features.team_manage.repository.TeamMemberRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamPermissionService {

    private final TeamMemberRepository teamMemberRepository;

    /** 팀장 또는 매니저 권한 확인 */
    public void checkLeaderOrManager(Long teamId, Long userId) {
        var member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRole() != TeamMember.Role.LEADER &&
                member.getRole() != TeamMember.Role.MANAGER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
    }
}
