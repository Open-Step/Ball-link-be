package com.openstep.balllinkbe.features.team_join.repository;

import com.openstep.balllinkbe.domain.team.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InviteRepository extends JpaRepository<Invite, String> {
    List<Invite> findByTeamId(Long teamId);
}
