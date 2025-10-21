package com.openstep.balllinkbe.features.tournament.repository;

import com.openstep.balllinkbe.domain.tournament.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByStatus(Tournament.Status status);
}
