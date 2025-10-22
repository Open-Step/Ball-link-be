package com.openstep.balllinkbe.domain.team;

import com.openstep.balllinkbe.domain.team.enums.Position;
import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "players",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_players_number_active",
                columnNames = {"team_id", "number", "is_active"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private Short number;   // smallint 매핑

    @Enumerated(EnumType.STRING)
    private Position position;

    private String note;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;  // boolean → Boolean (Wrapper type)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
