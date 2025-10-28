package com.openstep.balllinkbe.features.team_manage.repository;

import com.openstep.balllinkbe.domain.team.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamIdAndDeletedAtIsNull(Long teamId);
    Optional<Player> findByIdAndTeamId(Long id, Long teamId);
    boolean existsByTeamIdAndNumberAndIsActiveTrue(Long teamId, int number);
    long countByTeamIdAndIsActiveTrue(Long teamId);

    long countByTeamIdAndDeletedAtIsNull(Long teamId);
}