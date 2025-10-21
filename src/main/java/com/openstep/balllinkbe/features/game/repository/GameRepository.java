package com.openstep.balllinkbe.features.game.repository;

import com.openstep.balllinkbe.domain.game.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByTournamentIdOrderByScheduledAtAsc(Long tournamentId);
}
