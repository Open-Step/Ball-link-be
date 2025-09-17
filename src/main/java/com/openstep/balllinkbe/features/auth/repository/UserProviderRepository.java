package com.openstep.balllinkbe.features.auth.repository;

import com.openstep.balllinkbe.domain.user.UserProvider;
import com.openstep.balllinkbe.domain.user.UserProvider.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProviderRepository extends JpaRepository<UserProvider, Long> {
    Optional<UserProvider> findByProviderAndProviderUserId(Provider provider, String providerUserId);
}
