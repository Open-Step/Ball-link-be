package com.openstep.balllinkbe.features.game.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 경기 수정 */
@Getter
@Setter
public class UpdateGameRequest {
    private Long venueId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;
    private String roundCode;
    private Integer bracketOrder;
}
