package com.openstep.balllinkbe.domain.score;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "score_sessions",
        uniqueConstraints = @UniqueConstraint(name = "uk_score_session_token", columnNames = {"session_token"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScoreSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "game_id")
    private Game game;

    @Column(length = 64)
    private String sessionToken;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status { ACTIVE, EXPIRED, CLOSED }
}
