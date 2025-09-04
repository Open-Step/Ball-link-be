package com.openstep.balllinkbe.domain.team;

import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "join_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JoinRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "applicant_user_id")
    private User applicant;

    @Enumerated(EnumType.STRING)
    private Position position;

    private String location;
    private String bio;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "invite_code")
    private Invite invite;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime appliedAt;
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "processed_by")
    private User processedBy;

    private String rejectReason;

    public enum Position { PG, SG, SF, PF, C }
    public enum Status { PENDING, ACCEPTED, REJECTED }
}
