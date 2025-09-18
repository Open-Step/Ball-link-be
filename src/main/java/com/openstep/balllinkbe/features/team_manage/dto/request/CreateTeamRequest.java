package com.openstep.balllinkbe.features.team_manage.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTeamRequest {
    private String name;          // 팀명
    private String shortName;     // 팀 약칭
    private Short foundedYear;  // 창단년도
    private String region;        // 활동 지역
    private String ownerName;     // 대표자 이름 (DB에는 저장X, 응답용/프론트 표시용)
    private String description;   // 소개
    private String emblemFileId;  // 엠블럼 파일 ID
    private String colorPrimary;  // 팀 대표 색상
    private Boolean isPublic;     // 공개 여부 (추가 컬럼 고려)
}
