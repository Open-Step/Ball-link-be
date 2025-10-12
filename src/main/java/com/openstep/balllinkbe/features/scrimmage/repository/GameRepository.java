package com.openstep.balllinkbe.features.scrimmage.repository;

import com.openstep.balllinkbe.domain.game.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    // 삭제가 안된 자체전들 기간내 전체 조회 (페이징)
    @Query("SELECT g FROM Game g " +
            "WHERE g.isScrimmage = true " +
            "AND g.state = :state " +
            "AND g.homeTeam.id = :teamId " +
            "AND g.scheduledAt BETWEEN :startDate AND :endDate " +
            "AND g.deletedAt IS NULL")
    Page<Game> findScrimmagesByTeamAndDateRange(
            @Param("teamId") Long teamId,
            @Param("state") Game.State state,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
