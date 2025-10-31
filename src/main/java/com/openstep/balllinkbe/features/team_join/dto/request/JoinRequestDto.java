package com.openstep.balllinkbe.features.team_join.dto.request;

import com.openstep.balllinkbe.domain.team.enums.Position;
import lombok.Getter;
import lombok.Setter;

/**
 * 팀 가입 신청 요청 DTO
 * - 초대코드가 없으면 공개팀 지원, 있으면 비공개팀 초대 신청
 */
@Getter
@Setter
public class JoinRequestDto {
    private Long teamId;         // ✅ 팀 ID
    private Position position;   // ✅ 공통 enum (PG, SG, SF, PF, C)
    private String location;
    private String bio;
    private String inviteCode;   // ✅ 초대코드 (null 허용)
}
