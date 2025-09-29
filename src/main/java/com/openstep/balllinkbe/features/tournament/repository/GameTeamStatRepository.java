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

    @Query("""
        select new com.openstep.balllinkbe.features.tournament.dto.response.TeamRecordDto(
            g.tournament.id,
            g.tournament.name,

            count(distinct g.id),
             null ,null,

            coalesce(sum(s.pts),0),
            coalesce(sum(s.ast),0),
            coalesce(sum(s.reb),0),
            coalesce(sum(s.stl),0),
            coalesce(sum(s.blk),0),
            0,
            0,
            0
        )
        from GameTeamStat s
        join s.game g
        where s.team.id = :teamId
        group by g.tournament.id
        order by g.tournament.id
        """)
    Page<TeamRecordDto> findTeamRecordsByTournamentId(Long teamId, Pageable pageable);
}
