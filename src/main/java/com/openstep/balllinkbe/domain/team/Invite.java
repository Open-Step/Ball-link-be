package com.openstep.balllinkbe.domain.team;

import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "invites")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
public class Invite {

    @Id
    @Column(length = 32)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    // 무제한 정책 → nullable 허용
    private LocalDateTime expiresAt;
    private Integer maxUses;
    private Integer usedCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime revokedAt;

    public enum Status {
        ACTIVE, EXPIRED, REVOKED   // DB 정의 순서와 맞춤
    }
}
