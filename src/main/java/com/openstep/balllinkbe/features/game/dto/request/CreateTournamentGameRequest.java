package com.openstep.balllinkbe.features.game.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/** 대회 경기 생성 */
@Getter @Setter
public class CreateTournamentGameRequest {
    private Long homeTeamId;      // 필수
    private Long awayTeamId;      // 필수
    private Long venueId;         // 선택
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt; // 필수
    private String roundCode;     // GROUP|ROUND_OF_16|QF|SF|FINAL (선택)
    private Integer bracketOrder; // 선택
}

