package com.openstep.balllinkbe.features.auth.service;

import com.openstep.balllinkbe.domain.user.PasswordResetToken;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.auth.dto.request.LoginRequest;
import com.openstep.balllinkbe.features.auth.dto.request.SignupRequest;
import com.openstep.balllinkbe.features.auth.dto.response.AuthResponse;
import com.openstep.balllinkbe.features.auth.repository.AuthRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import com.openstep.balllinkbe.global.security.JwtTokenProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EntityManager em; // PasswordResetToken 저장/조회용
    private final JavaMailSender mailSender;

    /** 회원가입 */
    public Long signup(SignupRequest request) {
        if (authRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        authRepository.save(user);
        return user.getId();
    }

    /** 로그인 */
    public AuthResponse login(LoginRequest request) {
        User user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // name, profileImagePath, isAdmin 모두 포함한 JWT 생성
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

    /** 비밀번호 초기화 요청 (이메일 발송) */
    @Transactional
    public String requestPasswordReset(String email) {
        authRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        em.persist(resetToken);

        // 이메일 발송
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("비밀번호 재설정 안내");
            helper.setText("아래 토큰을 사용해 비밀번호를 재설정하세요:\n\n" + token, false);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 발송 실패", e);
        }

        return token; // 운영에서는 return 안 하고, 메일만 발송
    }

    /** 비밀번호 재설정 */
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        PasswordResetToken resetToken = em.createQuery(
                        "select t from PasswordResetToken t where t.token = :token and t.used = false",
                        PasswordResetToken.class
                )
                .setParameter("token", token)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 사용된 토큰입니다."));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("토큰이 만료되었습니다.");
        }

        User user = authRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        authRepository.save(user);

        resetToken.setUsed(true);
        em.merge(resetToken);
    }

    /** 유저 ID로 이메일 조회 (refresh API 용) */
    public String findEmailByUserId(Long userId) {
        return authRepository.findById(userId)
                .map(User::getEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /** 추가: isAdmin 조회 */
    public boolean isAdminByUserId(Long userId) {
        return authRepository.findById(userId)
                .map(User::isAdmin)
                .orElse(false);
    }

    public User findByUserId(Long userId) {
        return authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        var user = authRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (oldPassword.equals(newPassword)) {
            throw new CustomException(ErrorCode.SAME_PASSWORD_NOT_ALLOWED);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        authRepository.save(user);
    }


}
