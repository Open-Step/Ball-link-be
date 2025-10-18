package com.openstep.balllinkbe.features.team_record.repository.projection;

public interface PlayerReboundProjection {
    Long getPlayerId();
    Integer getOffReb(); // 공격 리바운드
    Integer getDefReb(); // 수비 리바운드
}
