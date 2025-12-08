package com.library.security;

import java.security.Principal;
import java.util.UUID;

/**
 * Gateway 인증 후 전달되는 사용자 정보를 담는 불변 객체.
 * SecurityContext의 Authentication principal로 사용됩니다.
 */
public record UserPrincipal(UUID userId, String role) implements Principal {

    /**
     * Principal 인터페이스 구현.
     * authentication.getName() 호출 시 userId를 문자열로 반환합니다.
     */
    @Override
    public String getName() {
        return userId.toString();
    }

    /**
     * ROLE_ 접두사가 붙은 권한 문자열을 반환합니다.
     */
    public String getAuthority() {
        return "ROLE_" + role;
    }
}