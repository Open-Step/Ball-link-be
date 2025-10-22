package com.openstep.balllinkbe.features.team_manage.dto.response;

import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.domain.team.enums.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "팀 멤버 응답 DTO")
public class TeamMemberResponse {

    private Long userId;
    private String name;
    private String email;
    private String role;
    private String profileImageUrl;

    @Schema(description = "등번호")
    private Integer backNumber;

    @Schema(description = "포지션 (PG, SG, SF, PF, C 중 하나)")
    private Position position;

    @Schema(description = "로케이션 (주로 뛰는 지역)")
    private String location;

    private LocalDateTime joinedAt;

    public static TeamMemberResponse from(TeamMember m) {
        return TeamMemberResponse.builder()
                .userId(m.getUser().getId())
                .name(m.getUser().getName())
                .email(m.getUser().getEmail())
                .role(m.getRole().name())
                .profileImageUrl(m.getUser().getProfileImagePath())
                .backNumber(m.getBackNumber())
                .position(m.getPosition())
                .location(m.getLocation())
                .joinedAt(m.getJoinedAt())
                .build();
    }
}
