package com.openstep.balllinkbe.domain.score;

import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "score_sessions")
public class ScoreSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 실제 DB에는 game_id는 FK가 아닌 일반 bigint 컬럼
    @Column(name = "game_id", nullable = false)
    private Long gameId;

    // ❌ DB에 없음 — 제거
    // private Long tournamentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // ✅ 필드명과 컬럼명 일치시키기
    @Column(name = "session_token", nullable = false, unique = true, length = 128)
    private String sessionToken;

    private LocalDateTime createdAt;

    // ❌ DB에 없음 — 제거
    // private LocalDateTime endedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // ✅ DB에 존재하는 expires_at 컬럼 추가
}
