package com.openstep.balllinkbe.features.tournament.repository;

import com.openstep.balllinkbe.domain.tournament.TournamentTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {
}
