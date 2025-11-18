package com.openstep.balllinkbe.features.tournament.repository;

import com.openstep.balllinkbe.domain.game.GameLineupPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameLineupPlayerRepository extends JpaRepository<GameLineupPlayer, Long> {
    void deleteByGameId(Long gameId);

    List<GameLineupPlayer> findByGameId(Long gameId);
}
