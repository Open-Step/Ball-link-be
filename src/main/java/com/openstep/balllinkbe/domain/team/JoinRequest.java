package com.openstep.balllinkbe.domain.team;

import com.openstep.balllinkbe.domain.team.enums.Position;
import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "join_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
public class JoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_user_id")
    private User applicant;

    /** 공통 Position enum 사용 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;

    private String location;
    private String bio;

    /** Invite가 실제 존재하므로 그대로 유지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invite_code")
    private Invite invite; // 공개팀이면 null 허용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @CreatedDate
    private LocalDateTime appliedAt;

    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    private String rejectReason;

    public enum Status { PENDING, ACCEPTED, REJECTED }
}
