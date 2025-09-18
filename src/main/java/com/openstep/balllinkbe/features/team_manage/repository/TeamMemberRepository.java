package com.openstep.balllinkbe.features.team_manage.repository;

import com.openstep.balllinkbe.domain.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamIdAndLeftAtIsNull(Long teamId);
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
}