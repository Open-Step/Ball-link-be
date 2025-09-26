package com.openstep.balllinkbe.features.team_join.repository;

import com.openstep.balllinkbe.domain.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamJoinMemberRepository extends JpaRepository<TeamMember, Long> {
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
}
