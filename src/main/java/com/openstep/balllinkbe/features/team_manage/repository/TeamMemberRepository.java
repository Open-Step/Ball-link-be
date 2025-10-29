package com.openstep.balllinkbe.features.team_manage.repository;

import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    // 특정 팀의 활동중인 멤버 조회
    List<TeamMember> findByTeamIdAndLeftAtIsNull(Long teamId);

    // 특정 유저가 팀에 속해있는지 여부 확인
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    // 팀 + 유저로 멤버 조회 (탈퇴/위임 처리 시 필요)
    Optional<TeamMember> findByTeamAndUser(Team team, User user);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    long countByTeamIdAndRoleAndLeftAtIsNull(Long teamId, TeamMember.Role role);

    List<TeamMember> findByUserIdAndLeftAtIsNull(Long userId);

    boolean existsByTeamIdAndUserIdAndLeftAtIsNull(Long teamId, Long userId);

    long countByTeamIdAndLeftAtIsNull(Long teamId);
}
