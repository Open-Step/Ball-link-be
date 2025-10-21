package com.openstep.balllinkbe.features.tournament.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateTournamentRequest {
    private String name;
    private String location;
    private String season;
    private String startDate;
    private String endDate;
    private String status; // SCHEDULED, ONGOING, FINISHED, CANCELED
}
