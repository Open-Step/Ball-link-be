package com.openstep.balllinkbe.features.score.repository;

import com.openstep.balllinkbe.domain.score.ScoreSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScoreSessionRepository extends JpaRepository<ScoreSession, Long> {

    @Query("""
        select s
        from ScoreSession s
        left join fetch s.createdBy u
        where s.gameId = :gameId
          and s.sessionToken = :token
          and (
                (:status = 'ACTIVE' and s.expiresAt is null)
             or (:status <> 'ACTIVE' and s.expiresAt is not null)
          )
    """)
    Optional<ScoreSession> findByGameIdAndSessionTokenAndStatus(
            @Param("gameId") Long gameId,
            @Param("token")  String token,
            @Param("status") String status
    );

    Optional<ScoreSession> findByGameId(Long gameId);
}
