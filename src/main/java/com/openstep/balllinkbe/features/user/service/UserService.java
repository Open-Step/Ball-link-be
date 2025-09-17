package com.openstep.balllinkbe.features.user.service;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthRepository authRepository;

    @Transactional
    public void updateEmail(Long userId, String email) {
        User user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setEmail(email);
        authRepository.save(user);
    }
}
