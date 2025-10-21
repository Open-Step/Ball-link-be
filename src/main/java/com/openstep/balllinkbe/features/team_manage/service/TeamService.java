package com.openstep.balllinkbe.features.team_manage.service;

import com.openstep.balllinkbe.domain.file.FileMeta;
import com.openstep.balllinkbe.domain.file.FileStorageService;
import com.openstep.balllinkbe.domain.file.FileValidator;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.team_manage.dto.request.CreateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.dto.request.UpdateTeamRequest;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamDetailResponse;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamResponse;
import com.openstep.balllinkbe.features.team_manage.dto.response.TeamSummaryResponse;
import com.openstep.balllinkbe.features.team_manage.repository.PlayerRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamMemberRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import com.openstep.balllinkbe.features.user.repository.UserRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final FileStorageService fileStorageService;

    /** 내가 가입한 팀 목록 (최대 3개) */
    public List<TeamSummaryResponse> getMyTeams(User currentUser) {
        var memberships = teamMemberRepository.findByUserIdAndLeftAtIsNull(currentUser.getId());

        return memberships.stream()
                .map(TeamMember::getTeam)
                .limit(3)
                .map(team -> new TeamSummaryResponse(
                        team,
                        toCdnUrl(team.getEmblemUrl()),
                        team.getOwnerUser() != null && team.getOwnerUser().getId().equals(currentUser.getId()) // ✅ isOwner
                ))
                .toList();
    }


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
        team.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : true);
        team.setOwnerUser(owner);
        team.setCreatedAt(LocalDateTime.now());

        String tag = generateUniqueTeamTag(dto.getName());
        team.setTeamTag(tag);

        Team saved = teamRepository.save(team);

        TeamMember member = new TeamMember();
        member.setTeam(saved);
        member.setUser(owner);
        member.setRole(TeamMember.Role.LEADER);
        teamMemberRepository.save(member);

        return saved.getId();
    }

    /** 고유 4자리 태그 생성 */
    private String generateUniqueTeamTag(String teamName) {
        String tag;
        do {
            tag = String.format("%04d", (int) (Math.random() * 10000));
        } while (teamRepository.existsByNameAndTeamTag(teamName, tag));
        return tag;
    }

    /** 팀 수정 (owner만 가능) */
    @Transactional
    public TeamResponse updateTeam(Long teamId, UpdateTeamRequest dto, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        if (team.getOwnerUser() == null || !team.getOwnerUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        if (dto.getDescription() != null) team.setDescription(dto.getDescription());
        if (dto.getFoundedAt() != null) team.setFoundedAt(dto.getFoundedAt());
        if (dto.getIsPublic() != null) team.setIsPublic(dto.getIsPublic());

        team.setUpdatedAt(LocalDateTime.now());
        Team updated = teamRepository.save(team);

        return new TeamResponse(updated, toCdnUrl(updated.getEmblemUrl()));
    }

    /** 팀 목록 조회 */
    public Page<TeamSummaryResponse> getTeams(int page, int size, String sort, String q) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort.split(",")));
        Page<Team> teams = (q != null && !q.isBlank())
                ? teamRepository.findByNameContainingAndIsPublicTrue(q, pageable)
                : teamRepository.findByIsPublicTrue(pageable);

        return teams.map(team ->
                new TeamSummaryResponse(
                        team,
                        toCdnUrl(team.getEmblemUrl()),
                        false // 공개 팀 목록에서는 오너 여부 계산 불필요, 항상 false로
                )
        );
    }

    /** 팀 상세 조회 */
    public TeamDetailResponse getTeamDetail(Long teamId, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        long playerCount = playerRepository.countByTeamIdAndIsActiveTrue(teamId);

        boolean isOwner = currentUser != null && team.getOwnerUser() != null &&
                team.getOwnerUser().getId().equals(currentUser.getId());

        String ownerProfileUrl = team.getOwnerUser() != null
                ? toCdnUrl(team.getOwnerUser().getProfileImagePath())
                : null;

        return new TeamDetailResponse(team, playerCount, toCdnUrl(team.getEmblemUrl()), isOwner, ownerProfileUrl);
    }


    /** 팀 삭제 */
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

        var member = teamMemberRepository.findByTeamAndUser(team, currentUser)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!team.getOwnerUser().getId().equals(currentUser.getId())) {
            teamMemberRepository.delete(member);
            return;
        }

        if (transferToUserId != null) {
            User newOwner = userRepository.findById(transferToUserId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            var newOwnerMember = teamMemberRepository.findByTeamAndUser(team, newOwner)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            team.setOwnerUser(newOwner);
            newOwnerMember.setRole(TeamMember.Role.LEADER);
            member.setRole(TeamMember.Role.PLAYER);

            teamRepository.save(team);
            teamMemberRepository.save(newOwnerMember);
            teamMemberRepository.save(member);
        } else {
            team.setDeletedAt(LocalDateTime.now());
            teamRepository.save(team);
        }
    }

    /** 팀 엠블럼 변경 */
    @Transactional
    public String updateTeamEmblem(Long teamId, MultipartFile file, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.getOwnerUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        FileValidator.validateImageFile(file);

        try {
            String relativePath = fileStorageService.storeFile(
                    teamId,
                    FileMeta.OwnerType.TEAM,
                    FileMeta.FileCategory.EMBLEM,
                    file.getOriginalFilename(),
                    file.getBytes()
            );

            team.setEmblemUrl(relativePath);
            team.setUpdatedAt(LocalDateTime.now());
            teamRepository.save(team);

            return fileStorageService.toCdnUrl(relativePath);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /** 공통 CDN URL 변환 */
    private String toCdnUrl(String relativePath) {
        return relativePath != null ? fileStorageService.toCdnUrl(relativePath) : null;
    }
}
