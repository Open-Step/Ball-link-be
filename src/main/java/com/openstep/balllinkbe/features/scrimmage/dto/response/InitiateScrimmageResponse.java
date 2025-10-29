package com.openstep.balllinkbe.features.scrimmage.dto.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class InitiateScrimmageResponse {
    private Long gameId;
    private String sessionToken;
}
