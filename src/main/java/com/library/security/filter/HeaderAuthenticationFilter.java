package com.library.security.filter;

import com.library.security.SecurityHeaders;
import com.library.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Gateway 헤더 기반 인증 필터.
 * Gateway에서 JWT 검증 후 주입한 X-User-Id, X-User-Role 헤더를 SecurityContext의 Authentication으로 변환합니다.
 * Order(1)로 최우선 실행되며, 헤더가 없으면 다음 필터로 위임합니다.
 */
@Slf4j
@Order(1)
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader(SecurityHeaders.USER_ID);
        String userRole = request.getHeader(SecurityHeaders.USER_ROLE);

        if (userId != null && userRole != null) {
            try {
                UUID userUuid = UUID.fromString(userId);
                UserPrincipal principal = new UserPrincipal(userUuid, userRole);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority(principal.getAuthority()))
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Gateway header authentication successful - UserId: {}, Role: {}",
                          userId, userRole);

            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format in {} header: {} - Authentication skipped",
                         SecurityHeaders.USER_ID, userId);
            }
        } else {
            log.debug("Gateway headers not present - Delegating to next filter");
        }

        filterChain.doFilter(request, response);
    }
}