package com.openstep.balllinkbe.features.team_join.repository;

import com.openstep.balllinkbe.domain.team.JoinRequest;
import com.openstep.balllinkbe.domain.team.JoinRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findByTeamIdAndStatus(Long teamId, Status status);
}
