package com.openstep.balllinkbe.features.user.controller;

import com.openstep.balllinkbe.features.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "user-controller", description = "사용자 정보 관리 API")
public class UserController {

    private final UserService userService;

    @PutMapping("/email")
    @Operation(summary = "이메일 변경", description = "현재 로그인한 사용자의 이메일 주소를 수정합니다.")
    public ResponseEntity<?> updateEmail(
            @RequestAttribute("userId") Long userId,
            @RequestBody com.openstep.balllinkbe.features.user.dto.request.UpdateEmailRequest request
    ) {
        userService.updateEmail(userId, request.getEmail());
        return ResponseEntity.ok().build();
    }
}
