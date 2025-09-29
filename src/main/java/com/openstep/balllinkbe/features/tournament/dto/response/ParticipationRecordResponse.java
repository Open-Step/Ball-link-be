package com.openstep.balllinkbe.features.tournament.dto.response;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public class ParticipationRecordResponse {
    //{ "tournamentId":31,"teamId":17,"games":8,"wins":6,"losses":2,"totals":{...},"perGame":{...} }
    private final Long tournamentId;
    private final Long teamId;
    private final int games;
    private final int wins;
    private final int losses;

}
