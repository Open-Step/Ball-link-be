package com.openstep.balllinkbe.features.team_record.repository.projection;

public interface TeamPeriodScoreProjection {
    Long getTeamId();
    Integer getPeriod(); // 1,2,3,4, (그 외=OT)
    Integer getPts();
}
