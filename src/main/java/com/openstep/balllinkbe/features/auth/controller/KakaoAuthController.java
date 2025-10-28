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

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/kakao")
@RequiredArgsConstructor
@Tag(name = "kakao-controller", description = "카카오 로그인 API (인가코드 → JWT 토큰 교환)")
public class KakaoAuthController {

    private final KakaoOAuthService kakaoOAuthService;

    /**
     * 프론트엔드에서 전달받은 인가코드(code)를 사용해
     * 카카오 서버에 액세스 토큰을 요청하고, JWT 토큰을 발급합니다.
     */
    @GetMapping("/callback")
    @Operation(
            summary = "카카오 토큰 교환",
            description = """
        프론트에서 받은 카카오 인가코드(code)를 이용해
        카카오 서버에 액세스 토큰을 요청하고 JWT 토큰(access/refresh)을 발급합니다.
        """
    )
    public ResponseEntity<?> exchangeCodeForJwt(
            @RequestParam String code,
            HttpServletResponse response
    ) {
        // 카카오 인증 서버로 요청하여 access/refresh 토큰 생성
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
        return ResponseEntity.ok(Map.of("accessToken", tokens.getAccessToken()));
    }
}
