package com.openstep.balllinkbe.features.team_manage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransferOwnershipRequest {
    @Schema(description = "팀장 권한을 위임할 대상 userId", example = "123")
    private Long toUserId;
}