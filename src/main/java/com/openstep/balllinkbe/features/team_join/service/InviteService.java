package com.openstep.balllinkbe.features.team_join.service;

import com.openstep.balllinkbe.domain.team.Invite;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_join.repository.InviteRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import com.openstep.balllinkbe.features.user.repository.UserRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import com.openstep.balllinkbe.global.security.TeamPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamPermissionService permissionService;

    /** 안전한 초대 코드 생성 (중복 방지) */
    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        } while (inviteRepository.existsById(code));
        return code;
    }

    /** 초대 생성 */
    public Invite createInvite(Long teamId, Long userId) {
        permissionService.checkLeaderOrManager(teamId, userId); // 권한 체크

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String code = generateUniqueCode();

        Invite invite = Invite.builder()
                .code(code)
                .team(team)
                .createdBy(user)
                .status(Invite.Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        return inviteRepository.save(invite);
    }

    /** 초대 검증 */
    public Invite validateInvite(String code) {
        return inviteRepository.findById(code)
                .filter(inv -> inv.getStatus() == Invite.Status.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE));
    }

    /** 초대 목록 조회 */
    public List<Invite> listInvites(Long teamId, Long userId) {
        permissionService.checkLeaderOrManager(teamId, userId); // 권한 체크
        return inviteRepository.findByTeamId(teamId);
    }

    /** 초대 회수 */
    public void revokeInvite(String code, Long teamId, Long userId) {
        permissionService.checkLeaderOrManager(teamId, userId); // 권한 체크

        Invite invite = inviteRepository.findById(code)
                .orElseThrow(() -> new CustomException(ErrorCode.INVITE_NOT_FOUND));

        if (invite.getStatus() != Invite.Status.ACTIVE) {
            throw new CustomException(ErrorCode.INVITE_ALREADY_REVOKED);
        }

        invite.setStatus(Invite.Status.REVOKED);
        invite.setRevokedAt(LocalDateTime.now());
        inviteRepository.save(invite);
    }
}
