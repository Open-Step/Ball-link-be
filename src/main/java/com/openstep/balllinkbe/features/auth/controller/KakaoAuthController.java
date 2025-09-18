package com.openstep.balllinkbe.features.auth.controller;

import com.openstep.balllinkbe.features.auth.dto.response.AuthResponse;
import com.openstep.balllinkbe.features.auth.service.KakaoOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth/kakao")
@RequiredArgsConstructor
@Tag(name = "kakao-controller", description = "카카오 로그인 API")
public class KakaoAuthController {

    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/callback")
    @Operation(summary = "카카오 로그인 콜백", description = "카카오 인증 서버에서 전달받은 code를 이용해 액세스 토큰과 리프레시 토큰을 발급받습니다.")
    public void kakaoCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        AuthResponse tokens = kakaoOAuthService.loginWithKakao(code);
        String redirectUrl = "/index.html?accessToken=" + tokens.getAccessToken()
                + "&refreshToken=" + tokens.getRefreshToken();
        response.sendRedirect(redirectUrl);
    }

}
