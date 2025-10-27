package com.openstep.balllinkbe.features.auth.controller;

import com.openstep.balllinkbe.features.auth.dto.request.*;
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
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // accessToken만 JSON 응답으로 반환
        return ResponseEntity.ok(Map.of("accessToken", tokens.getAccessToken()));
    }


    /** 로그아웃 */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "refreshToken 쿠키를 제거합니다.")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // refreshToken 쿠키 즉시 만료시켜 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .maxAge(0) // 즉시 만료
                .httpOnly(true)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok(Map.of("success", true, "message", "로그아웃 되었습니다."));
    }


    /** 토큰 재발급 */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 검증 후 새로운 액세스 토큰을 발급합니다.")
    public ResponseEntity<Map<String, String>> refresh(
            @CookieValue(value = "refreshToken") String refreshToken,
            HttpServletResponse response) {

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        var user = authService.findByUserId(userId);

        // 새로운 accessToken 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.isAdmin(),
                user.getName(),
                user.getProfileImagePath()
        );

        // refreshToken은 그대로 유지하되, 다시 쿠키로 세팅
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경용 (로컬테스트 시 false로 변경 가능)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // body에는 accessToken만 반환
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }


    /** 비밀번호 재설정 요청 */
    @PostMapping("/password-reset")
    @Operation(summary = "비밀번호 재설정 요청", description = "사용자 이메일을 입력받아 비밀번호 재설정 토큰을 발급하고, 이메일로 발송합니다.")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        String token = authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.accepted().body(Map.of("success", true, "token", token));
    }

    /** 비밀번호 재설정 확인 */
    @PostMapping("/password-reset/confirm")
    @Operation(summary = "비밀번호 재설정 확인", description = "발급된 토큰과 새 비밀번호를 입력받아 비밀번호를 실제로 변경합니다.")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("success", true));
    }


    /** 비밀번호 변경 */
    @PostMapping("/password-change")
    @Operation(summary = "비밀번호 변경", description = "로그인된 사용자가 기존 비밀번호를 검증하고 새 비밀번호로 변경합니다.")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authorization) {

        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        // 액세스 토큰에서 사용자 ID 추출
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserId(token);

        authService.changePassword(userId, oldPassword, newPassword);

        return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다."));
    }


}
