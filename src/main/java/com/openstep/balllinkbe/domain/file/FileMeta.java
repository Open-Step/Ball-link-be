package com.openstep.balllinkbe.domain.file;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor@Builder
public class FileMeta {
    @Id
    @Column(length = 36)
    private String fileId;

    private String originalName;
    private String contentType;
    private Long sizeBytes;

    private LocalDateTime createdAt;
}
