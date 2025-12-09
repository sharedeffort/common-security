# common-security

High Tension MSA 프로젝트의 공통 보안 모듈입니다.

## Features

- **UserPrincipal**: 사용자 정보를 담는 불변 객체 (UUID userId, String role)
- **HeaderAuthenticationFilter**: Gateway 헤더를 SecurityContext로 변환
- **SecurityContextUtil**: 현재 사용자 정보 조회 유틸리티

## Installation

### JitPack

build.gradle:
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.sharedeffort:common-security:v1.1.0'
}
```

## Usage

### SecurityContextUtil

#### Controller에서 사용자 ID 조회
```java
// Before (3 lines)
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String userIdStr = auth.getName();
UUID userId = UUID.fromString(userIdStr);

// After (1 line)
UUID userId = SecurityContextUtil.getCurrentUserId();
```

#### JPA Auditing 설정 (v1.1.0+)
```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return SecurityContextUtil::getCurrentUserIdForAuditing;
    }
}
```

**특징**:
- common-jpa BaseEntity UUID 타입 완벽 호환
- 인증 실패 시 `Optional.empty()` 반환 (예외 없음)
- 메서드 레퍼런스로 간결한 코드

### SecurityConfig Integration

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            );

        http.addFilterBefore(
            new HeaderAuthenticationFilter(),
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}
```

## API Reference

### SecurityContextUtil

| 메서드 | 반환 타입 | 인증 실패 시 | 사용 시나리오 |
|--------|----------|-------------|--------------|
| `getCurrentUserId()` | `UUID` | 예외 발생 | Controller, Service 로직 |
| `getCurrentUserIdForAuditing()` | `Optional<UUID>` | `Optional.empty()` | **JPA Auditing (권장)** |
| `getCurrentUserIdAsString()` | `String` | `"anonymous"` 반환 | 로깅, 외부 API |
| `getCurrentUserRole()` | `String` | 예외 발생 | 권한 확인 |
| `getCurrentUserPrincipal()` | `Optional<UserPrincipal>` | `Optional.empty()` | 안전한 접근 |

## Version History

- **v1.1.0**: JPA Auditing UUID support
  - `getCurrentUserIdForAuditing()` 메서드 추가
  - common-jpa UUID 타입 완벽 호환
- **v1.0.0**: Initial release
  - UserPrincipal, HeaderAuthenticationFilter, SecurityContextUtil