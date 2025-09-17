package com.openstep.balllinkbe.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;   // 인증 토큰 (UUID)

    @Column(nullable = false)
    private String email;   // 대상 이메일

    @Column(nullable = false)
    private LocalDateTime expiresAt;  // 만료 시간

    private boolean used;   // 사용 여부
}
