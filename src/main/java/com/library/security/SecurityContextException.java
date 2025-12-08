package com.library.security;

/**
 * SecurityContext에서 인증 정보를 가져올 수 없을 때 발생하는 예외.
 */
public class SecurityContextException extends RuntimeException {

    /**
     * 메시지를 포함한 예외를 생성합니다.
     */
    public SecurityContextException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함한 예외를 생성합니다.
     */
    public SecurityContextException(String message, Throwable cause) {
        super(message, cause);
    }
}