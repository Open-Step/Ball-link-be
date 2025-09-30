package com.openstep.balllinkbe.features.tournament.repository;

import com.openstep.balllinkbe.domain.game.GameTeamStat;
import com.openstep.balllinkbe.features.tournament.dto.response.TeamRecordDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GameTeamStatRepository extends JpaRepository<GameTeamStat, Long> {

    @Query( value = """
              select new com.openstep.balllinkbe.features.tournament.dto.response.TeamRecordDto(
                cast(g.tournament.id as Long),
                g.tournament.name,
            
                cast(count(distinct g.id) as Long),
                cast(null as Long),
                cast(null as Long),
            
                coalesce(sum(s.pts), 0L),
                coalesce(sum(s.ast), 0L),
                coalesce(sum(s.reb), 0L),
                coalesce(sum(s.stl), 0L),
                coalesce(sum(s.blk), 0L),
                0L, 0L, 0L
              )
              from GameTeamStat s
              join s.game g
              where s.team.id = :teamId
              group by g.tournament.id, g.tournament.name
              order by g.tournament.id
            """,
        countQuery = """
          select count(distinct g.tournament.id)
          from GameTeamStat s
          join s.game g
          where s.team.id = :teamId
    """)
    Page<TeamRecordDto> findTeamRecordsByTournamentId(Long teamId, Pageable pageable);
}
