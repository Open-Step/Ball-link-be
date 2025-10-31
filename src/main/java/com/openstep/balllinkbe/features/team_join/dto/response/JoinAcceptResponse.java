package com.openstep.balllinkbe.features.team_join.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JoinAcceptResponse {
    private Long userId;   // 가입 승인된 사용자 ID
    private Long playerId; // 새로 생성된 Player ID
    private Long memberId; // TeamMember ID
}
