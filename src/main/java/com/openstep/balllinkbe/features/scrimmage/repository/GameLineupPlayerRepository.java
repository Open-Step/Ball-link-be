package com.openstep.balllinkbe.features.scrimmage.repository;

import com.openstep.balllinkbe.domain.game.GameLineupPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameLineupPlayerRepository extends JpaRepository<GameLineupPlayer, Long> {
}
