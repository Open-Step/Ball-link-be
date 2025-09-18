package com.openstep.balllinkbe.features.team_manage.repository;

import com.openstep.balllinkbe.domain.tournament.TournamentEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentEntryRepository extends JpaRepository<TournamentEntry, Long> {
    boolean existsByTournamentIdAndTeamIdAndNumber(Long tournamentId, Long teamId, int number);
}
