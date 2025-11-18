package com.openstep.balllinkbe.features.score.repository;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GamePlayerStat;
import com.openstep.balllinkbe.domain.team.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GamePlayerStatScoreRepository extends JpaRepository<GamePlayerStat, Long> {

    @Modifying
    @Query("""
        UPDATE GamePlayerStat s 
        SET s.pts = s.pts + :pts 
        WHERE s.game.id = :gameId AND s.player.id = :playerId
    """)
    void incrementPoints(@Param("gameId") Long gameId,
                         @Param("playerId") Long playerId,
                         @Param("pts") int pts);

    @Modifying
    @Query("""
        UPDATE GamePlayerStat s 
        SET s.pf = s.pf + 1 
        WHERE s.game.id = :gameId AND s.player.id = :playerId
    """)
    void incrementFouls(@Param("gameId") Long gameId,
                        @Param("playerId") Long playerId);

    Optional<GamePlayerStat> findByGameAndPlayer(Game game, Player player);
    void deleteByGameId(Long gameId);
    Optional<GamePlayerStat> findByGameAndPlayerId(Game game, Long playerId);
}
