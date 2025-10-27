package com.openstep.balllinkbe.features.scrimmage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "자체전(스크림) 생성 요청 DTO")
public class CreateScrimmageRequest {

    @Schema(description = "홈팀 ID", example = "1")
    @NotNull
    private Long homeTeamId;

    @Schema(description = "원정팀 ID", example = "2")
    @NotNull
    private Long awayTeamId;
}
