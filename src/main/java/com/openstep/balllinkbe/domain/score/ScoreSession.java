package com.openstep.balllinkbe.domain.score;

import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "score_sessions")
public class ScoreSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "tournament_id")
    private Long tournamentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, unique = true, length = 128)
    private String token;

    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}
