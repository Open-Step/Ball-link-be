package com.openstep.balllinkbe.features.team_manage.dto.request;

import com.openstep.balllinkbe.domain.team.enums.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "팀 멤버 권한/정보 수정 요청 DTO")
public class UpdateRoleRequest {

    @Schema(description = "변경할 역할 (LEADER, MANAGER, PLAYER)")
    private String role;

    @Schema(description = "등번호")
    private Integer backNumber;

    @Schema(description = "포지션 (PG, SG, SF, PF, C 중 하나)", example = "SG")
    private Position position;

    @Schema(description = "활동 지역")
    private String location;
}
