package com.openstep.balllinkbe.features.score.repository;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameTeamStat;
import com.openstep.balllinkbe.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameTeamStatScoreRepository extends JpaRepository<GameTeamStat, Long> {

    @Modifying
    @Query("""
        UPDATE GameTeamStat s 
        SET s.pts = s.pts + :pts 
        WHERE s.game.id = :gameId AND s.team.id = :teamId
    """)
    void incrementPoints(@Param("gameId") Long gameId,
                         @Param("teamId") Long teamId,
                         @Param("pts") int pts);

    @Modifying
    @Query("""
        UPDATE GameTeamStat s 
        SET s.pf = s.pf + 1 
        WHERE s.game.id = :gameId AND s.team.id = :teamId
    """)
    void incrementFouls(@Param("gameId") Long gameId,
                        @Param("teamId") Long teamId);

    Optional<GameTeamStat> findByGameAndTeam(Game game, Team team);

    void deleteByGameId(Long gameId);
}
