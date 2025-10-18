package com.openstep.balllinkbe.features.team_record.repository.projection;

public interface PlayerPeriodScoreProjection {
    Long getPlayerId();
    Long getTeamId();
    Integer getPeriod();  // 1,2,3,4, (5=OT처럼 사용)
    Integer getPts();
}
