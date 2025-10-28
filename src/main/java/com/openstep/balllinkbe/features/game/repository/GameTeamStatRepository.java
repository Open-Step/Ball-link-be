package com.openstep.balllinkbe.features.game.repository;

import com.openstep.balllinkbe.domain.game.GameTeamStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameTeamStatRepository extends JpaRepository<GameTeamStat, Long> {
    Optional<GameTeamStat> findByGameIdAndTeamId(Long gameId, Long teamId);
}
