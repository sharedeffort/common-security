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
    implementation 'com.github.sharedeffort:common-security:v1.0.0'
}
```

## Usage

### SecurityContextUtil

```java
// Before (3 lines)
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String userIdStr = auth.getName();
UUID userId = UUID.fromString(userIdStr);

// After (1 line)
UUID userId = SecurityContextUtil.getCurrentUserId();
```

### SecurityConfig Integration

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(headerAuthenticationFilter,
                           UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

## Version

- v1.0.0: Initial release