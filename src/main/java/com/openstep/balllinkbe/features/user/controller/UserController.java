package com.openstep.balllinkbe.features.user.controller;

import com.openstep.balllinkbe.features.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/email")
    public ResponseEntity<?> updateEmail(
            @RequestAttribute("userId") Long userId,
            @RequestBody com.openstep.balllinkbe.features.user.dto.request.UpdateEmailRequest request
    ) {
        userService.updateEmail(userId, request.getEmail());
        return ResponseEntity.ok().build();
    }
}
