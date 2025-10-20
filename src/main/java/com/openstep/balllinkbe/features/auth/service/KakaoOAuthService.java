package com.openstep.balllinkbe.features.auth.service;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.domain.user.UserProvider;
import com.openstep.balllinkbe.features.auth.dto.response.AuthResponse;
import com.openstep.balllinkbe.features.auth.dto.response.KakaoTokenResponse;
import com.openstep.balllinkbe.features.auth.dto.response.KakaoUserResponse;
import com.openstep.balllinkbe.features.auth.repository.AuthRepository;
import com.openstep.balllinkbe.features.auth.repository.UserProviderRepository;
import com.openstep.balllinkbe.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final AuthRepository authRepository;
    private final UserProviderRepository userProviderRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret:}") // 선택값
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /** 카카오 로그인 플로우 */
    public AuthResponse loginWithKakao(String code) {
        KakaoTokenResponse tokenResponse = requestToken(code);
        KakaoUserResponse kakaoUser = requestUserInfo(tokenResponse.getAccessToken());

        User user = findOrCreateUser(kakaoUser);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.isAdmin());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken);
    }

    /** 카카오 인가코드 → 토큰 교환 */
    private KakaoTokenResponse requestToken(String code) {
        String url = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        if (clientSecret != null && !clientSecret.isBlank()) {
            params.add("client_secret", clientSecret);
        }

        return restTemplate.postForObject(url, new HttpEntity<>(params, headers), KakaoTokenResponse.class);
    }

    /** 카카오 액세스 토큰 → 사용자 정보 조회 */
    private KakaoUserResponse requestUserInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), KakaoUserResponse.class).getBody();
    }

    /** DB 조회 or 신규 유저 생성 */
    private User findOrCreateUser(KakaoUserResponse kakaoUser) {
        Optional<UserProvider> up = userProviderRepository.findByProviderAndProviderUserId(
                UserProvider.Provider.KAKAO,
                String.valueOf(kakaoUser.getId())
        );

        if (up.isPresent()) {
            return up.get().getUser();
        }

        User newUser = User.builder()
                .email(kakaoUser.getKakaoAccount().getEmail())
                .name(kakaoUser.getKakaoAccount().getProfile().getNickname())
                .gender("male".equals(kakaoUser.getKakaoAccount().getGender()) ? User.Gender.M : User.Gender.F)
                .birthYear(kakaoUser.getKakaoAccount().getBirthyear() != null
                        ? Short.valueOf(kakaoUser.getKakaoAccount().getBirthyear()) : null)
                .phone(kakaoUser.getKakaoAccount().getPhoneNumber())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        authRepository.save(newUser);

        UserProvider provider = UserProvider.builder()
                .user(newUser)
                .provider(UserProvider.Provider.KAKAO)
                .providerUserId(String.valueOf(kakaoUser.getId()))
                .email(kakaoUser.getKakaoAccount().getEmail())
                .createdAt(LocalDateTime.now())
                .build();

        userProviderRepository.save(provider);

        return newUser;
    }
}
