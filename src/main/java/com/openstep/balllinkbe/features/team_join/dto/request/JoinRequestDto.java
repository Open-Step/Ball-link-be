package com.openstep.balllinkbe.features.team_join.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class JoinRequestDto {
    private Long teamId;
    private String position;
    private String location;
    private String bio;
    private String inviteCode; // 공개팀이면 null 가능
}