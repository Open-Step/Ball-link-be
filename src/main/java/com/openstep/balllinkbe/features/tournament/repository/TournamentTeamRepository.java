package com.openstep.balllinkbe.features.tournament.repository;

import com.openstep.balllinkbe.domain.tournament.TournamentTeam;
import com.openstep.balllinkbe.domain.tournament.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {
    List<TournamentTeam> findByTournament(Tournament tournament);
    Optional<TournamentTeam> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);
    boolean existsByTournamentIdAndTeamId(Long tournamentId, Long teamId);
    int countByTournamentId(Long tournamentId);
}
