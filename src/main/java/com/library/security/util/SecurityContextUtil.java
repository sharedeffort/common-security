package com.library.security.util;

import com.library.security.SecurityContextException;
import com.library.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * SecurityContext 접근 유틸리티.
 * Controller 및 Service에서 현재 인증된 사용자 정보를 간편하게 조회합니다.
 */
@Slf4j
public final class SecurityContextUtil {

    private SecurityContextUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 현재 인증된 사용자의 UUID를 반환합니다.
     * 인증되지 않은 경우 SecurityContextException이 발생합니다.
     */
    public static UUID getCurrentUserId() {
        return getCurrentUserPrincipal()
                .map(UserPrincipal::userId)
                .orElseThrow(() -> new SecurityContextException(
                        "No authenticated user found in SecurityContext"));
    }

    /**
     * 현재 인증된 사용자의 역할을 반환합니다.
     * 인증되지 않은 경우 SecurityContextException이 발생합니다.
     */
    public static String getCurrentUserRole() {
        return getCurrentUserPrincipal()
                .map(UserPrincipal::role)
                .orElseThrow(() -> new SecurityContextException(
                        "No authenticated user found in SecurityContext"));
    }

    /**
     * 현재 인증된 사용자의 UserPrincipal을 Optional로 반환합니다.
     * 인증되지 않은 경우 Optional.empty()를 반환합니다.
     */
    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
            !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        // Case 1: UserPrincipal
        if (principal instanceof UserPrincipal userPrincipal) {
            return Optional.of(userPrincipal);
        }

        // Case 2: UUID (기존 User Service 호환)
        if (principal instanceof UUID uuid) {
            String role = extractRoleFromAuthentication(authentication);
            return Optional.of(new UserPrincipal(uuid, role));
        }

        // Case 3: String (fallback)
        if (principal instanceof String userIdString) {
            try {
                UUID userId = UUID.fromString(userIdString);
                String role = extractRoleFromAuthentication(authentication);
                return Optional.of(new UserPrincipal(userId, role));
            } catch (IllegalArgumentException e) {
                log.warn("Principal is not a valid UUID: {}", userIdString);
                return Optional.empty();
            }
        }

        log.warn("Unknown principal type: {}", principal.getClass().getName());
        return Optional.empty();
    }

    /**
     * 현재 사용자의 userId를 String으로 반환합니다.
     * JpaAuditingConfig의 AuditorAware에서 사용하기 적합합니다.
     * 인증되지 않은 경우 anonymous를 반환합니다.
     */
    public static String getCurrentUserIdAsString() {
        return getCurrentUserPrincipal()
                .map(UserPrincipal::getName)
                .orElse("anonymous");
    }

    // Authentication에서 역할 추출
    private static String extractRoleFromAuthentication(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> {
                    String auth = authority.getAuthority();
                    return auth.startsWith("ROLE_") ? auth.substring(5) : auth;
                })
                .orElse("USER");
    }
}