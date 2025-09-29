package com.openstep.balllinkbe.features.user.dto.response;

import com.openstep.balllinkbe.domain.user.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final String profileImageUrl; // CDN URL 적용

    public UserResponse(User user, String cdnUrl) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.profileImageUrl = cdnUrl;
    }
}
