package com.openstep.balllinkbe.features.team_record.repository.projection;

import java.math.BigDecimal;

public interface PlayerLineProjection {
    Long getTeamId();
    Long getPlayerId();
    String getPlayerName();
    Integer getBackNumber();
    String getPosition();
    Integer getPts();
    Integer getReb();
    Integer getAst();
    Integer getStl();
    Integer getBlk();
    Integer getFg2Made();
    Integer getFg2Att();
    Integer getFg3Made();
    Integer getFg3Att();
    Integer getFtMade();
    Integer getFtAtt();
    Integer getPf();
    Integer getTov();
    BigDecimal getMinutes();
}
