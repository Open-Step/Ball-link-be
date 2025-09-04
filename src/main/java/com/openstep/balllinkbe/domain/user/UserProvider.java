package com.openstep.balllinkbe.domain.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_providers",
        uniqueConstraints = @UniqueConstraint(name = "uk_provider_user", columnNames = {"provider", "providerUserId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProvider {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(nullable = false, length = 191)
    private String providerUserId;

    private String email;
    private LocalDateTime createdAt;

    public enum Provider { EMAIL, KAKAO, GOOGLE, APPLE }
}
