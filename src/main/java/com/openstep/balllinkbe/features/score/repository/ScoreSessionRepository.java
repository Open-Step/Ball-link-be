package com.openstep.balllinkbe.features.score.repository;

import com.openstep.balllinkbe.domain.score.ScoreSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ScoreSessionRepository extends JpaRepository<ScoreSession, Long> {

    @Query("""
        SELECT s
        FROM ScoreSession s
        JOIN FETCH s.createdBy u
        JOIN FETCH s.game g
        WHERE g.id = :gameId
          AND s.sessionToken = :token
          AND s.status = 'ACTIVE'
    """)
    Optional<ScoreSession> findByGameIdAndSessionTokenAndStatus(Long gameId, String token, String status);
}
