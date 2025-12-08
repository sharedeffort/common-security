package com.library.security;

/**
 * Gateway에서 백엔드 서비스로 전달되는 보안 헤더 상수.
 */
public final class SecurityHeaders {

    public static final String USER_ID = "X-User-Id";
    public static final String USER_ROLE = "X-User-Role";

    private SecurityHeaders() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}