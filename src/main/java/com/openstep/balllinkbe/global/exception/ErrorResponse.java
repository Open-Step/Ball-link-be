package com.openstep.balllinkbe.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;   // ErrorCode 이름 또는 예외명
    private final String message; // 상세 메시지
    private final String path;    // 요청 경로
}
