package com.openstep.balllinkbe.domain.team;

import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "teams",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_team_name_tag", columnNames = {"name", "team_tag"})
        }
)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "team_tag", length = 4, nullable = false)
    private String teamTag;   // 팀 고유 태그 (0001~9999)

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

    @Column(name = "emblem_url", length = 500)
    private String emblemUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /** ID-only 생성자 (프록시용) */
    public Team(Long id) {
        this.id = id;
    }

    /** -------------------------
     *        BUILDER 추가
     *  -------------------------
     */
    @Builder
    public Team(
            Long id,
            String name,
            String teamTag,
            String shortName,
            LocalDate foundedAt,
            String region,
            String description,
            Boolean isPublic,
            String emblemUrl,
            User ownerUser,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        this.id = id;
        this.name = name;
        this.teamTag = teamTag;
        this.shortName = shortName;
        this.foundedAt = foundedAt;
        this.region = region;
        this.description = description;
        this.isPublic = isPublic;
        this.emblemUrl = emblemUrl;
        this.ownerUser = ownerUser;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }
}
