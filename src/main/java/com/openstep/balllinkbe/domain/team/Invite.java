package com.openstep.balllinkbe.domain.team;

import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invites")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invite {
    @Id
    @Column(length = 32)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime expiresAt;
    private Integer maxUses;
    private Integer usedCount;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime revokedAt;

    public enum Status { ACTIVE, REVOKED, EXPIRED }
}
