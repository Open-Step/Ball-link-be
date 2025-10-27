package com.openstep.balllinkbe.features.scrimmage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "게스트 추가 요청 DTO")
public class CreateGuestRequest {

    @Schema(description = "이름", example = "게스트홍")
    private String name;

    @Schema(description = "등번호", example = "99")
    private Integer number;

    @Schema(description = "포지션", example = "SF")
    private String position;

    @Schema(description = "소속 팀 (HOME / AWAY)", example = "HOME")
    private String teamSide;
}
