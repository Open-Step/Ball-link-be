package com.openstep.balllinkbe.features.team_join.repository;

import com.openstep.balllinkbe.domain.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamJoinMemberRepository extends JpaRepository<TeamMember, Long> {
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long id);
}
