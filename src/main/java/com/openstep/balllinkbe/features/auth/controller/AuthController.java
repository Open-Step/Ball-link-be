package com.openstep.balllinkbe.features.auth.controller;

import com.openstep.balllinkbe.features.auth.dto.request.LoginRequest;
import com.openstep.balllinkbe.features.auth.dto.request.SignupRequest;
import com.openstep.balllinkbe.features.auth.dto.response.AuthResponse;
import com.openstep.balllinkbe.features.auth.service.AuthService;
import com.openstep.balllinkbe.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "auth-controller", description = "로그인 관련 API (회원가입, 로그인, 토큰 등)")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    /** 회원가입 */
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 이메일, 비밀번호, 이름 등의 정보가 필요합니다.")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    /** 로그인 */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하면 accessToken을 응답하며, refreshToken은 HttpOnly 쿠키로 발급합니다.")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse tokens = authService.login(request);

        // refreshToken을 HttpOnly 쿠키로 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // accessToken만 JSON 응답으로 반환
        return ResponseEntity.ok(Map.of("accessToken", tokens.getAccessToken()));
    }


    /** 로그아웃 */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자의 세션/토큰을 만료 처리합니다.")
    public ResponseEntity<?> logout(@RequestAttribute("userId") Long userId) {
        // TODO: refreshToken 무효화 (DB/Redis 저장 시)
        return ResponseEntity.ok().build();
    }

    /** 토큰 재발급 */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 검증 후 새로운 액세스 토큰을 발급합니다.")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build(); // RefreshToken 만료/위조
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String email = authService.findEmailByUserId(userId);

        boolean isAdmin = authService.isAdminByUserId(userId);

        String newAccessToken = jwtTokenProvider.createAccessToken(userId, email, isAdmin);
        return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
    }

    /** 비밀번호 재설정 요청 */
    @PostMapping("/password-reset")
    @Operation(summary = "비밀번호 재설정 요청", description = "사용자 이메일을 입력받아 비밀번호 재설정 토큰을 발급하고, 이메일로 발송합니다.")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String token = authService.requestPasswordReset(email);
        return ResponseEntity.accepted().body(Map.of("success", true, "token", token));
    }

    /** 비밀번호 재설정 확인 */
    @PostMapping("/password-reset/confirm")
    @Operation(summary = "비밀번호 재설정 확인", description = "발급된 토큰과 새 비밀번호를 입력받아 비밀번호를 실제로 변경합니다.")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        authService.confirmPasswordReset(token, newPassword);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
