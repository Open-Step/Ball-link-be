package com.openstep.balllinkbe.domain.team;

import com.openstep.balllinkbe.domain.file.FileMeta;
import com.openstep.balllinkbe.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teams")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_user_id")
    private User owner;

    @Column(nullable = false, length = 120)
    private String name;

    private String shortName;
    private Short foundedYear;
    private String region;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "emblem_file_id")
    private FileMeta emblem;

    private String colorPrimary;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
