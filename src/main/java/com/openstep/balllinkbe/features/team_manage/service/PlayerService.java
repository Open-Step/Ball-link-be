package com.openstep.balllinkbe.features.team_manage.service;

import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.CreatePlayerRequest;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdatePlayerRequest;
import com.openstep.balllinkbe.features.team_manage.dto.response.PlayerResponse;
import com.openstep.balllinkbe.features.team_manage.repository.PlayerRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamMemberRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TournamentEntryRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TournamentEntryRepository tournamentEntryRepository;

    /** 선수 등록 */
    public Long createPlayer(Long teamId, CreatePlayerRequest request, User currentUser) {
        // 팀장/매니저 권한 검증
        var currentMember = teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (currentMember.getRole() == null ||
                (currentMember.getRole() != TeamMember.Role.LEADER &&
                        currentMember.getRole() != TeamMember.Role.MANAGER)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        Player player = Player.builder()
                .team(new Team(teamId)) // ID-only 생성자
                .user(request.getUserId() != null ? new User(request.getUserId()) : null)
                .name(request.getName())
                .number(request.getNumber())
                .position(Player.Position.valueOf(request.getPosition()))
                .note(request.getNote())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        return playerRepository.save(player).getId();
    }

    /** 선수 목록 조회 */
    public List<PlayerResponse> getPlayers(Long teamId) {
        return playerRepository.findByTeamIdAndDeletedAtIsNull(teamId).stream()
                .map(PlayerResponse::new)
                .toList();
    }

    /** 선수 수정 */
    public void updatePlayer(Long teamId, Long playerId, UpdatePlayerRequest request, User currentUser) {
        // 팀장/매니저 권한 검증
        var currentMember = teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (currentMember.getRole() == null ||
                (currentMember.getRole() != TeamMember.Role.LEADER &&
                        currentMember.getRole() != TeamMember.Role.MANAGER)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        var player = playerRepository.findByIdAndTeamId(playerId, teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYER_NOT_FOUND));

        if (request.getNumber() != null) player.setNumber(request.getNumber());
        if (request.getPosition() != null) player.setPosition(Player.Position.valueOf(request.getPosition()));
        if (request.getNote() != null) player.setNote(request.getNote());
        player.setUpdatedAt(LocalDateTime.now());

        playerRepository.save(player);
    }

    /** 선수 삭제 */
    public void deletePlayer(Long teamId, Long playerId, User currentUser) {
        // 팀장/매니저 권한 검증
        var currentMember = teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (currentMember.getRole() == null ||
                (currentMember.getRole() != TeamMember.Role.LEADER &&
                        currentMember.getRole() != TeamMember.Role.MANAGER)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        var player = playerRepository.findByIdAndTeamId(playerId, teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYER_NOT_FOUND));

        player.setDeletedAt(LocalDateTime.now());
        player.setIsActive(false);
        playerRepository.save(player);
    }

    /** 등번호 중복 확인 */
    public boolean validateNumber(Long teamId, Short number, String scope, Long tournamentId) {
        if ("TEAM".equalsIgnoreCase(scope)) {
            return !playerRepository.existsByTeamIdAndNumberAndIsActiveTrue(teamId, number);
        } else if ("TOURNAMENT".equalsIgnoreCase(scope) && tournamentId != null) {
            return !tournamentEntryRepository.existsByTournamentIdAndTeamIdAndNumber(tournamentId, teamId, number);
        }
        return true;
    }

    /** 멤버를 선수로 편입 */
    public Long addFromMember(Long teamId, CreatePlayerRequest request, User currentUser) {
        // 팀장/매니저 권한 검증
        var currentMember = teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (currentMember.getRole() == null ||
                (currentMember.getRole() != TeamMember.Role.LEADER &&
                        currentMember.getRole() != TeamMember.Role.MANAGER)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        var member = teamMemberRepository.findByTeamIdAndUserId(teamId, request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Player player = Player.builder()
                .team(member.getTeam())
                .user(member.getUser())
                .name(member.getUser().getName())
                .number(request.getNumber())
                .position(Player.Position.valueOf(request.getPosition()))
                .note(request.getNote())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        return playerRepository.save(player).getId();
    }
}
