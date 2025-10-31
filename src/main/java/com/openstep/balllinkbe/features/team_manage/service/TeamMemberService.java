package com.openstep.balllinkbe.features.team_manage.service;

import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdateRoleRequest;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamMemberResponse;
import com.openstep.balllinkbe.features.team_manage.repository.PlayerRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamMemberRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    /** 팀 멤버 목록 조회 (비공개 접근제한 포함, 비페이징 버전) */
    public List<TeamMemberResponse> getMembers(Long teamId, User currentUser) {
        // 1. 팀 정보 조회
        var team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        // 2. 접근 제어
        if (!Boolean.TRUE.equals(team.getIsPublic())) { // 비공개 팀이면
            boolean isMember = teamMemberRepository.existsByTeamIdAndUserIdAndLeftAtIsNull(teamId, currentUser.getId());
            if (!isMember) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED);
            }
        }

        // 3. 멤버 조회
        var members = teamMemberRepository.findByTeamIdAndLeftAtIsNull(teamId);

        // 4. DTO 변환 후 반환
        return members.stream()
                .map(TeamMemberResponse::from)
                .toList();
    }


    /** 멤버 정보 수정 (권한 + 등번호 + 포지션 + 로케이션)
     *  팀장(LEADER), 매니저(MANAGER)만 수정 가능
     */
    @Transactional
    public void updateRole(Long teamId, Long userId, UpdateRoleRequest request, User currentUser) {

        // 1. 수정 요청자 검증
        var currentMember = teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (currentMember.getRole() != TeamMember.Role.LEADER &&
                currentMember.getRole() != TeamMember.Role.MANAGER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        // 2. 수정 대상 조회
        var member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 3. 권한(role) 변경 (필요 시)
        if (request.getRole() != null && !request.getRole().isBlank()) {
            TeamMember.Role newRole = TeamMember.Role.valueOf(request.getRole().toUpperCase());

            // 매니저 인원 제한 검증 (최대 2명)
            if (newRole == TeamMember.Role.MANAGER) {
                long managerCount = teamMemberRepository.countByTeamIdAndRoleAndLeftAtIsNull(teamId, TeamMember.Role.MANAGER);
                if (managerCount >= 2 && member.getRole() != TeamMember.Role.MANAGER) {
                    throw new CustomException(ErrorCode.INVALID_REQUEST);
                }
            }

            member.setRole(newRole);
        }

        // 4. 등번호 / 포지션 / 로케이션 수정
        if (request.getBackNumber() != null) member.setBackNumber(request.getBackNumber());
        if (request.getPosition() != null) member.setPosition(request.getPosition());
        if (request.getLocation() != null) member.setLocation(request.getLocation());

        // 5. 저장
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

    /** 팀원 강퇴 로직 */
    @Transactional
    public void kickMember(Long teamId, Long userId, User currentUser) {
        // 현재 사용자 권한 확인
        var currentMember = teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (currentMember.getRole() == null ||
                (currentMember.getRole() != TeamMember.Role.LEADER &&
                        currentMember.getRole() != TeamMember.Role.MANAGER)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        // 강퇴 대상 확인
        var target = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 자기 자신 강퇴 방지
        if (target.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 팀장 강퇴 금지
        if (target.getRole() == TeamMember.Role.LEADER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        // 팀원 탈퇴 처리
        target.setLeftAt(LocalDateTime.now());
        teamMemberRepository.save(target);

        // 플레이어 엔티티도 비활성화 (같은 유저가 등록된 경우)
        playerRepository.findByTeamIdAndUserId(teamId, userId)
                .ifPresent(player -> {
                    player.setDeletedAt(LocalDateTime.now());
                    player.setIsActive(false);
                    playerRepository.save(player);
                });
    }
}
