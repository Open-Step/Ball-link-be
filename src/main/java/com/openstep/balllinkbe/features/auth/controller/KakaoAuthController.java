package com.openstep.balllinkbe.features.auth.controller;

import com.openstep.balllinkbe.features.auth.dto.response.AuthResponse;
import com.openstep.balllinkbe.features.auth.service.KakaoOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth/kakao")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/callback")
    public void kakaoCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        AuthResponse tokens = kakaoOAuthService.loginWithKakao(code);
        String redirectUrl = "/index.html?accessToken=" + tokens.getAccessToken()
                + "&refreshToken=" + tokens.getRefreshToken();
        response.sendRedirect(redirectUrl);
    }

}
