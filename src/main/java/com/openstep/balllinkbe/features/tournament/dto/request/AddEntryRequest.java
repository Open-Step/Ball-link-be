package com.openstep.balllinkbe.features.tournament.dto.request;

import com.openstep.balllinkbe.domain.team.enums.Position;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddEntryRequest {
    private Long playerId;
    private Short number;
    @Enumerated(EnumType.STRING)
    private Position position;
}
