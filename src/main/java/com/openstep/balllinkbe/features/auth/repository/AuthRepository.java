package com.openstep.balllinkbe.features.auth.repository;

import com.openstep.balllinkbe.domain.user.PasswordResetToken;
import com.openstep.balllinkbe.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
