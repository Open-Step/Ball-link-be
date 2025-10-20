package com.openstep.balllinkbe.domain.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Short birthYear;
    private String phone;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 전역 관리자 여부 (true면 시스템 관리자)
    @Column(nullable = false)
    @Builder.Default
    private boolean isAdmin = false;

    public enum Gender { M, F }

    /** ID-only 생성자 (FK 매핑용) */
    public User(Long id) {
        this.id = id;
    }

    private String profileImagePath; // DB에는 상대경로 저장
}
