package com.openstep.balllinkbe.features.auth.service;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.domain.user.UserProvider;
import com.openstep.balllinkbe.features.auth.dto.response.AuthResponse;
import com.openstep.balllinkbe.features.auth.dto.response.KakaoTokenResponse;
import com.openstep.balllinkbe.features.auth.dto.response.KakaoUserResponse;
import com.openstep.balllinkbe.features.auth.repository.AuthRepository;
import com.openstep.balllinkbe.features.auth.repository.UserProviderRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import com.openstep.balllinkbe.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
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
    private String redirectUri; // ⚠️ 프론트 콜백 주소 (/auth/kakao/callback)

    /**
     * 프론트에서 전달받은 인가코드(code)를 사용해 카카오 서버에서 토큰을 받고,
     * 해당 유저의 JWT 토큰(access/refresh)을 발급한다.
     */
    public AuthResponse loginWithKakao(String code) {
        KakaoTokenResponse tokenResponse = requestToken(code);
        KakaoUserResponse kakaoUser = requestUserInfo(tokenResponse.getAccessToken());
        User user = findOrCreateUser(kakaoUser);

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.isAdmin(),
                user.getName(),
                user.getProfileImagePath()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken);
    }

    /**
     * 인가코드를 이용해 카카오로부터 Access Token, Refresh Token을 발급받는다.
     * redirect_uri는 반드시 프론트의 콜백 주소와 동일해야 한다.
     */
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

        try {
            return restTemplate.postForObject(
                    url,
                    new HttpEntity<>(params, headers),
                    KakaoTokenResponse.class
            );
        } catch (HttpClientErrorException.BadRequest e) {
            // redirect_uri 불일치, 잘못된 code 등
            String body = e.getResponseBodyAsString();
            if (body.contains("invalid_grant")) {
                throw new CustomException(ErrorCode.KAKAO_INVALID_CODE);
            } else if (body.contains("invalid_redirect")) {
                throw new CustomException(ErrorCode.KAKAO_INVALID_REDIRECT_URI);
            } else if (body.contains("invalid_client")) {
                throw new CustomException(ErrorCode.KAKAO_INVALID_CLIENT);
            }
            throw new CustomException(ErrorCode.KAKAO_TOKEN_EXCHANGE_FAILED);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new CustomException(ErrorCode.KAKAO_TOKEN_EXCHANGE_FAILED);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    /**
     * 카카오 Access Token으로 사용자 정보를 조회한다.
     */
    private KakaoUserResponse requestUserInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try {
            ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), KakaoUserResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new CustomException(ErrorCode.KAKAO_USERINFO_FAILED);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new CustomException(ErrorCode.KAKAO_USERINFO_FAILED);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    /**
     * DB에 기존 유저가 있으면 반환, 없으면 신규 생성
     */
    private User findOrCreateUser(KakaoUserResponse kakaoUser) {
        Optional<UserProvider> existing = userProviderRepository.findByProviderAndProviderUserId(
                UserProvider.Provider.KAKAO,
                String.valueOf(kakaoUser.getId())
        );

        if (existing.isPresent()) {
            return existing.get().getUser();
        }

        User newUser = User.builder()
                .email(kakaoUser.getKakaoAccount().getEmail())
                .name(kakaoUser.getKakaoAccount().getProfile().getNickname())
                .gender("male".equals(kakaoUser.getKakaoAccount().getGender())
                        ? User.Gender.M : User.Gender.F)
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
