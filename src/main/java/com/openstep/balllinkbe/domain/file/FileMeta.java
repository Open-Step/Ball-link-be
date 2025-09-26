package com.openstep.balllinkbe.domain.file;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMeta {

    @Id
    @Column(length = 36)
    private String fileId;  // UUID

    private String originalName;
    private String contentType;
    private Long sizeBytes;

    private String relativePath; // ex) teams/7/7_20250926132000.png

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OwnerType ownerType; // USER, TEAM, SCRIMMAGE 등

    private Long ownerId;        // ex) userId, teamId, gameId

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FileCategory category; // PROFILE, EMBLEM, RESULT

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // ---- 내부 Enum ----
    public enum OwnerType {
        USER, TEAM, SCRIMMAGE, TOURNAMENT
    }

    public enum FileCategory {
        PROFILE, EMBLEM, RESULT, DOC
    }
}
