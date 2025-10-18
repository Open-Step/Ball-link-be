package com.openstep.balllinkbe.features.game.repository;

import com.openstep.balllinkbe.domain.game.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> { }
