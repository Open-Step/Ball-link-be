package com.openstep.balllinkbe.features.auth.controller;

import com.openstep.balllinkbe.features.auth.dto.response.AuthResponse;
import com.openstep.balllinkbe.features.auth.service.KakaoOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/kakao")
@RequiredArgsConstructor
@Tag(name = "kakao-controller", description = "카카오 로그인 API")
public class KakaoAuthController {

    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/callback")
    @Operation(summary = "카카오 로그인 콜백", description = "카카오 인증 서버에서 전달받은 code를 이용해 JWT 토큰을 발급합니다.")
    public ResponseEntity<?> kakaoCallback(@RequestParam String code, HttpServletResponse response) {
        AuthResponse tokens = kakaoOAuthService.loginWithKakao(code);

        // refreshToken을 HttpOnly 쿠키로 발급
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // accessToken을 JSON 형태로 응답
        // isNew:True, False 추가
        return ResponseEntity.ok(Map.of(
                "accessToken", tokens.getAccessToken()
        ));
    }
}
