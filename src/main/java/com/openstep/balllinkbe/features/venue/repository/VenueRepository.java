package com.openstep.balllinkbe.features.venue.repository;

import com.openstep.balllinkbe.domain.venue.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
