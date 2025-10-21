package com.openstep.balllinkbe.features.tournament.dto.request;

import com.openstep.balllinkbe.domain.team.Player;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddEntryRequest {
    private Long playerId;
    private Short number;
    private Player.Position position;
}
