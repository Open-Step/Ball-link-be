package com.openstep.balllinkbe.domain.team;

import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "short_name", length = 255)
    private String shortName;

    @Column(name = "founded_at")
    private LocalDate foundedAt;

    @Column(length = 255)
    private String region;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    // emblemFile → emblemUrl 로 변경 (CDN URL만 저장)
    @Column(name = "emblem_url", length = 500)
    private String emblemUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
