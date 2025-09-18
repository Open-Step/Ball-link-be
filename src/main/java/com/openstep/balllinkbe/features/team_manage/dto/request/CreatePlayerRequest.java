package com.openstep.balllinkbe.features.team_manage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreatePlayerRequest {
    @Schema(description = "선수 이름", example = "홍길동")
    private String name;

    @Schema(description = "등번호", example = "23")
    private Short number; // Integer → Short

    @Schema(description = "포지션 (PG, SG, SF, PF, C)", example = "PG")
    private String position;

    @Schema(description = "연계 User ID (선택)", example = "7")
    private Long userId;

    @Schema(description = "비고", example = "MVP")
    private String note;
}
