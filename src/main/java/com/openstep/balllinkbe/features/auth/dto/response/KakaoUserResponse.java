package com.openstep.balllinkbe.features.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoUserResponse {
    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Data
    public static class KakaoAccount {
        private Profile profile;
        private String email;
        private String gender;     // male / female
        private String birthyear;  // ex) "1995"
        @JsonProperty("phone_number")
        private String phoneNumber;
    }

    @Data
    public static class Profile {
        private String nickname;
    }
}
