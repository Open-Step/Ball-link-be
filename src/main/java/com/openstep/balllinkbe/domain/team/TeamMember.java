package com.openstep.balllinkbe.domain.team;

import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_team_user", columnNames = {"team_id","user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamMember {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Role role;

    /** ✅ 등번호 (nullable 허용, 팀별 등록번호) */
    @Column(name = "back_number")
    private Integer backNumber;

    /** 포지션 (예: G, F, C 등) */
    @Column(length = 10)
    private String position;

    /** 주요 활동 지역 */
    @Column(length = 100)
    private String location;

    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;

    public enum Role { LEADER, MANAGER, PLAYER }
}
