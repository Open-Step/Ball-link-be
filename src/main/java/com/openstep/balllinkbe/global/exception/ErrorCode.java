package com.openstep.balllinkbe.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 인증/권한
    UNAUTHORIZED_MEMBER(HttpStatus.FORBIDDEN, "팀 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."), // 관리자 권한 부족 등 공통 사용
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "기존 비밀번호가 일치하지 않습니다."),
    SAME_PASSWORD_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "기존 비밀번호와 새 비밀번호가 동일할 수 없습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    // Team
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "팀 멤버를 찾을 수 없습니다."),
    PLAYER_NOT_FOUND(HttpStatus.NOT_FOUND, "선수를 찾을 수 없습니다."),
    DUPLICATE_TEAM_TAG(HttpStatus.CONFLICT, "팀 태그가 중복되었습니다."),
    MAX_TEAM_LIMIT(HttpStatus.BAD_REQUEST, "최대 가입 가능한 팀 수를 초과했습니다."),

    // Invite
    INVALID_INVITE(HttpStatus.BAD_REQUEST, "유효하지 않은 초대 코드입니다."),
    INVITE_NOT_FOUND(HttpStatus.NOT_FOUND, "초대 코드를 찾을 수 없습니다."),
    INVITE_ALREADY_REVOKED(HttpStatus.CONFLICT, "이미 회수된 초대 코드입니다."),

    // JoinRequest
    JOIN_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "가입 신청을 찾을 수 없습니다."),
    JOIN_REQUEST_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 가입 신청입니다."),

    // File
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "파일 크기가 5MB를 초과했습니다."),

    // Record
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "기록 정보를 찾을 수 없습니다."),
    RECORD_AGGREGATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "기록 집계 중 오류가 발생했습니다."),
    RECORD_INVALID_SEASON(HttpStatus.BAD_REQUEST, "유효하지 않은 시즌 값입니다."),

    TOURNAMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "대회를 찾을 수 없습니다."),
    VENUE_NOT_FOUND(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."),
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "경기를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
