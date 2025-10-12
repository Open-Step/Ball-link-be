package com.openstep.balllinkbe.features.scrimmage.repository;

import com.openstep.balllinkbe.domain.game.GameEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameEventRepository extends JpaRepository<GameEvent, Long> {
    List<GameEvent> findByGameIdOrderByTsAsc(Long gameId);
}
