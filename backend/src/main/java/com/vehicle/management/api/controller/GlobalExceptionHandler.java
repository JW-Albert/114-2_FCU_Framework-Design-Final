package com.vehicle.management.api.controller;

import com.vehicle.management.domain.state.InvalidStateTransitionException;
import com.vehicle.management.service.AccountLockedException;
import com.vehicle.management.service.ConflictException;
import com.vehicle.management.service.PermissionDeniedException;
import com.vehicle.management.service.ResourceNotFoundException;
import com.vehicle.management.service.WeakPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleForbidden(PermissionDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleConflict(ConflictException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, Object> handleInvalidState(InvalidStateTransitionException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleBadCredentials(BadCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(WeakPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleWeakPassword(WeakPasswordException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public Map<String, Object> handleAccountLocked(AccountLockedException ex) {
        return error(HttpStatus.LOCKED, ex.getMessage());
    }

    /**
     * 處理 {@code @Valid} 請求參數驗證失敗。
     *
     * <p>若不在此明確處理，Spring 會將 {@link MethodArgumentNotValidException}
     * 轉發至 {@code /error}，而 {@code JwtAuthFilter}（OncePerRequestFilter）不會在
     * ERROR dispatch 重跑，導致 SecurityContext 為空、回傳誤導的 401（前端因而誤登出）。
     * 在此攔截並回傳正確的 400，同時帶出各欄位的驗證訊息。</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message.isBlank() ? "請求參數驗證失敗" : message);
    }

    private Map<String, Object> error(HttpStatus status, String message) {
        return Map.of("status", status.value(), "error", message, "timestamp", Instant.now());
    }
}
