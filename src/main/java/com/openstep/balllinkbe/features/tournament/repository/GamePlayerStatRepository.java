package com.openstep.balllinkbe.features.tournament.repository;

import com.openstep.balllinkbe.domain.game.GamePlayerStat;
import com.openstep.balllinkbe.features.tournament.dto.response.PlayerRecordDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GamePlayerStatRepository extends JpaRepository<GamePlayerStat, Long> {
    @Query("""
        select new com.openstep.balllinkbe.features.tournament.dto.response.PlayerRecordDto(
          0,
          p.id,
          p.name,
          p.number,
          COUNT(DISTINCT gp.game.id),
          SUM(gp.pts),
          SUM(gp.ast),
          SUM(gp.reb),
          SUM(gp.stl),
          SUM(gp.blk),
          SUM(gp.fg2Made),
          SUM(gp.fg3Made),
          SUM(gp.ftMade)
        )
        from GamePlayerStat gp
        join gp.player p
        where gp.team.id = :teamId
        group by p.id, p.name, p.number
        order by SUM(:rankBy) desc
    """)
    Page<PlayerRecordDto> findPlayerRecordsByTeam(@Param("teamId") Long teamId,
                                                  @Param("rankBy") String rankBy,
                                                  Pageable pageable);
}
