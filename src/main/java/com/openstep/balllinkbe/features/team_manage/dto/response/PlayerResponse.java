package com.openstep.balllinkbe.features.team_manage.dto.response;

import com.openstep.balllinkbe.domain.team.Player;
import lombok.Getter;

@Getter
public class PlayerResponse {
    private final Long id;
    private final String name;
    private final Short number;
    private final String position;
    private final String note;

    public PlayerResponse(Player player) {
        this.id = player.getId();
        this.name = player.getName();
        this.number = player.getNumber();
        this.position = player.getPosition() != null ? player.getPosition().name() : null;
        this.note = player.getNote();
    }
}
