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
    private final String round; // ✅ 추가: "8강", "16강" 등

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
        this.round = calcRound(teamCount); // ✅ 자동 계산
    }

    // 기존 코드 호환용 생성자 (teamCount 기본 0)
    public TournamentResponse(Tournament t) {
        this(t, 0);
    }

    /** ✅ 팀 수에 따라 "강" 계산 */
    private String calcRound(int count) {
        if (count >= 64) return "64강";
        if (count >= 32) return "32강";
        if (count >= 16) return "16강";
        if (count >= 8)  return "8강";
        if (count >= 4)  return "4강";
        if (count >= 2)  return "결승";
        if (count == 1)  return "단독 참가";
        return "-";
    }
}
