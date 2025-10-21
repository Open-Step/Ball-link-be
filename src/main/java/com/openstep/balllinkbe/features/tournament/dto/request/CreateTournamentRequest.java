package com.openstep.balllinkbe.features.tournament.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateTournamentRequest {
    private String name;
    private String location;
    private String season;
    private String startDate; // yyyy-MM-dd
    private String endDate;   // yyyy-MM-dd
}
