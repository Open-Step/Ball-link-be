package com.openstep.balllinkbe.features.team_join.service;

import com.openstep.balllinkbe.domain.team.*;
import com.openstep.balllinkbe.domain.team.enums.Position;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_join.dto.response.JoinAcceptResponse;
import com.openstep.balllinkbe.features.team_join.dto.response.JoinRequestResponse;
import com.openstep.balllinkbe.features.team_join.repository.InviteRepository;
import com.openstep.balllinkbe.features.team_join.repository.JoinRequestRepository;
import com.openstep.balllinkbe.features.team_join.repository.TeamJoinMemberRepository;
import com.openstep.balllinkbe.features.team_manage.repository.PlayerRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import com.openstep.balllinkbe.features.user.repository.UserRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import com.openstep.balllinkbe.global.security.TeamPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JoinRequestService {

    private final JoinRequestRepository joinRequestRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    private final TeamJoinMemberRepository teamMemberRepository;
    private final TeamPermissionService permissionService;
    private final PlayerRepository playerRepository;
    /** 가입 신청 생성 */
    @Transactional
    public JoinRequest apply(Long teamId, Long userId, Position position, String location, String bio, String inviteCode) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Invite invite = null;
        if (!team.getIsPublic()) {
            invite = inviteRepository.findById(inviteCode)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE));
        }

        JoinRequest req = JoinRequest.builder()
                .team(team)
                .applicant(user)
                // enum 그대로 사용 (valueOf 불필요)
                .position(position)
                .location(location)
                .bio(bio)
                .invite(invite)
                .status(JoinRequest.Status.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();

        return joinRequestRepository.save(req);
    }

    /** 가입 신청 목록 조회 */
    public List<JoinRequestResponse> listRequests(Long teamId, Long userId, JoinRequest.Status status) {
        permissionService.checkLeaderOrManager(teamId, userId);
        return joinRequestRepository.findByTeamIdAndStatus(teamId, status)
                .stream()
                .map(JoinRequestResponse::new)
                .toList();
    }


    /** 가입 신청 수락 */
    @Transactional
    public JoinAcceptResponse accept(Long reqId, Long teamId, Long processedBy) {
        permissionService.checkLeaderOrManager(teamId, processedBy);

        JoinRequest req = joinRequestRepository.findById(reqId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        if (req.getStatus() != JoinRequest.Status.PENDING) {
            throw new CustomException(ErrorCode.JOIN_REQUEST_ALREADY_PROCESSED);
        }

        User approver = userRepository.findById(processedBy)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 🔹 member 변수를 if 밖에서 선언
        TeamMember member = teamMemberRepository
                .findByTeamIdAndUserId(teamId, req.getApplicant().getId())
                .orElse(null);

        // 팀 멤버 등록 (없을 경우만)
        if (member == null) {
            member = TeamMember.builder()
                    .team(req.getTeam())
                    .user(req.getApplicant())
                    .role(TeamMember.Role.PLAYER)
                    .joinedAt(LocalDateTime.now())
                    .build();
            teamMemberRepository.save(member);
        }

        // Player 생성 (중복 방지)
        Player player = playerRepository.findByTeamIdAndUserId(teamId, req.getApplicant().getId())
                .orElse(null);

        if (player == null) {
            player = Player.builder()
                    .team(req.getTeam())
                    .user(req.getApplicant())
                    .name(req.getApplicant().getName())
                    .position(req.getPosition())
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            playerRepository.save(player);
        }

        // 가입 요청 상태 갱신
        req.setStatus(JoinRequest.Status.ACCEPTED);
        req.setProcessedAt(LocalDateTime.now());
        req.setProcessedBy(approver);
        joinRequestRepository.save(req);

        return new JoinAcceptResponse(
                req.getApplicant().getId(),
                player.getId(),
                member.getId()
        );
    }


    /** 가입 신청 거절 */
    @Transactional
    public void reject(Long reqId, Long teamId, Long processedBy, String reason) {
        permissionService.checkLeaderOrManager(teamId, processedBy); // 권한 체크

        JoinRequest req = joinRequestRepository.findById(reqId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        if (req.getStatus() != JoinRequest.Status.PENDING) {
            throw new CustomException(ErrorCode.JOIN_REQUEST_ALREADY_PROCESSED);
        }

        req.setStatus(JoinRequest.Status.REJECTED);
        req.setRejectReason(reason);
        req.setProcessedAt(LocalDateTime.now());
        joinRequestRepository.save(req);
    }
}
