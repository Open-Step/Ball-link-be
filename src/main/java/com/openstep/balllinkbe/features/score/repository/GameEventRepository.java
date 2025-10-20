package com.openstep.balllinkbe.features.score.repository;

import com.openstep.balllinkbe.domain.game.GameEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GameEventRepository extends JpaRepository<GameEvent, Long> {
    List<GameEvent> findByGameIdOrderByTsAsc(Long gameId);

    @Query("""
    SELECT e 
    FROM GameEvent e 
    WHERE e.game.id = :gameId AND e.type = 'CLOCK_UPDATE' 
    ORDER BY e.ts DESC LIMIT 1
""")
    Optional<GameEvent> findLatestClockEvent(@Param("gameId") Long gameId);

    @Query(value = """
    SELECT period, SUM(CAST(JSON_UNQUOTE(JSON_EXTRACT(meta, '$.pts')) AS SIGNED)) AS pts
    FROM game_events 
    WHERE game_id = :gameId AND type = 'SCORE'
    GROUP BY period
    ORDER BY period
""", nativeQuery = true)
    List<Map<String, Object>> findQuarterScores(@Param("gameId") Long gameId);

    @Query("""
    SELECT e 
    FROM GameEvent e 
    WHERE e.game.id = :gameId AND e.type = 'PERIOD_START'
    ORDER BY e.ts DESC LIMIT 1
""")
    Optional<GameEvent> findLastStartedPeriod(@Param("gameId") Long gameId);

}
