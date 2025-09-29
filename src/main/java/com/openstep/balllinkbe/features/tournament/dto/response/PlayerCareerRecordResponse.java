package com.openstep.balllinkbe.features.tournament.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@AllArgsConstructor
@Builder
public class PlayerCareerRecordResponse {
    private Long teamId;
    private String season;
    private String split;
    private String rankBy;
    private List<PlayerRecordDto> items;
    private int page;
    private int size;
    private long total;
}
