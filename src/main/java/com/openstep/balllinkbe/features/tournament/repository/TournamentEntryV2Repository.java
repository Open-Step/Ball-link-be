package com.openstep.balllinkbe.features.tournament.repository;

import com.openstep.balllinkbe.domain.tournament.TournamentEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TournamentEntryV2Repository extends JpaRepository<TournamentEntry, Long> {
    List<TournamentEntry> findByTournamentId(Long tournamentId);
    void deleteByTournamentId(Long tournamentId);
}
