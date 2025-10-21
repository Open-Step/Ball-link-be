package com.openstep.balllinkbe.features.tournament.dto.response;

import com.openstep.balllinkbe.domain.tournament.Tournament;
import lombok.Getter;

@Getter
public class TournamentResponse {
    private final Long id;
    private final String name;
    private final String location;
    private final String season;
    private final String startDate;
    private final String endDate;
    private final String status;
    private final Integer teamCount;

    // 새 버전 (팀 수 포함)
    public TournamentResponse(Tournament t, int teamCount) {
        this.id = t.getId();
        this.name = t.getName();
        this.location = t.getLocation();
        this.season = t.getSeason();
        this.startDate = t.getStartDate() != null ? t.getStartDate().toString() : null;
        this.endDate = t.getEndDate() != null ? t.getEndDate().toString() : null;
        this.status = t.getStatus() != null ? t.getStatus().name() : null;
        this.teamCount = teamCount;
    }

    // 기존 코드 호환용 생성자 추가 (teamCount 기본 0)
    public TournamentResponse(Tournament t) {
        this(t, 0);
    }
}

