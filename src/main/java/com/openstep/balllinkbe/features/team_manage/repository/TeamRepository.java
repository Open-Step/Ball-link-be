package com.openstep.balllinkbe.features.team_manage.repository;

import com.openstep.balllinkbe.domain.team.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Page<Team> findByNameContainingAndIsPublicTrue(String q, Pageable pageable);

    Page<Team> findByIsPublicTrue(Pageable pageable);

    // 팀 이름과 teamTag 조합이 이미 존재하는지 확인
    boolean existsByNameAndTeamTag(String name, String teamTag);
}
