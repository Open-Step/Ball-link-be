package com.openstep.balllinkbe.features.team_manage.repository;

import com.openstep.balllinkbe.domain.team.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamIdAndDeletedAtIsNull(Long teamId);
    boolean existsByTeamIdAndNumberAndDeletedAtIsNull(Long teamId, Integer number);
}