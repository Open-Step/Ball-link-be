package com.openstep.balllinkbe.features.score.repository;

import com.openstep.balllinkbe.domain.score.ScoreSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScoreSessionRepository extends JpaRepository<ScoreSession, Long> {

    // 그대로 사용하던 메소드명 + 파라미터 유지
    @Query("""
        select s
        from ScoreSession s
        left join fetch s.createdBy u
        where s.gameId = :gameId
          and s.token  = :token
          and (
                (:status = 'ACTIVE' and s.endedAt is null)
             or (:status <> 'ACTIVE' and s.endedAt is not null)
          )
    """)
    Optional<ScoreSession> findByGameIdAndSessionTokenAndStatus(
            @Param("gameId") Long gameId,
            @Param("token")  String token,
            @Param("status") String status
    );

    Optional<ScoreSession> findByGameId(Long gameId);
}
