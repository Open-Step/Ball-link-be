package com.openstep.balllinkbe.features.auth.controller;

import com.openstep.balllinkbe.features.auth.dto.request.LoginRequest;
import com.openstep.balllinkbe.features.auth.dto.request.SignupRequest;
import com.openstep.balllinkbe.features.auth.dto.response.AuthResponse;
import com.openstep.balllinkbe.features.auth.service.AuthService;
import com.openstep.balllinkbe.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    /** 로그인 */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestAttribute("userId") Long userId) {
        // TODO: refreshToken 무효화 (DB/Redis 저장 시)
        return ResponseEntity.ok().build();
    }

    /** 토큰 재발급 */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build(); // RefreshToken 만료/위조
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        // 이메일 같은 부가정보가 필요하다면 DB 조회
        String email = authService.findEmailByUserId(userId);

        String newAccessToken = jwtTokenProvider.createAccessToken(userId, email);
        return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String token = authService.requestPasswordReset(email);
        // 운영에서는 token을 반환하지 않고, 메일만 전송
        return ResponseEntity.accepted().body(Map.of("success", true, "token", token));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        authService.confirmPasswordReset(token, newPassword);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
