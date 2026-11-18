package com.ecomm.product.exception;

import com.ecomm.common.dto.ProblemDetailsDto;
import com.ecomm.common.exception.BaseDomainException;
import com.ecomm.common.exception.MeaningfulErrorTranslator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise Global Exception Handler conforming to RFC 7807 Problem Details for HTTP APIs.
 * Works seamlessly with AspectJ Service/Controller aspects to ensure user-friendly,
 * actionable error messages with distributed tracing correlation IDs.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String MDC_CORRELATION_KEY = "correlationId";

    @ExceptionHandler(BaseDomainException.class)
    public ResponseEntity<ProblemDetailsDto> handleDomainException(BaseDomainException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        log.warn("[{}] Handled DomainException: [{}] {} on {}",
                correlationId, ex.getErrorCode(), ex.getMessage(), request.getRequestURI());

        HttpStatus status = HttpStatus.valueOf(ex.getHttpStatus());
        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/" + ex.getErrorCode().toLowerCase().replace('_', '-'))
                .title(status.getReasonPhrase())
                .status(status.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetailsDto> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        BaseDomainException translated = MeaningfulErrorTranslator.translateDataIntegrityViolation(ex);

        log.warn("[{}] DataIntegrityViolation translated to {}: {} on {}",
                correlationId, translated.getClass().getSimpleName(), translated.getMessage(), request.getRequestURI());

        HttpStatus status = HttpStatus.valueOf(translated.getHttpStatus());
        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/" + translated.getErrorCode().toLowerCase().replace('_', '-'))
                .title(status.getReasonPhrase())
                .status(status.value())
                .detail(translated.getMessage())
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailsDto> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("[{}] Validation failed on {}: {}", correlationId, request.getRequestURI(), errors);

        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/validation-failed")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("One or more fields failed validation checks. Please correct the values and retry.")
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetailsDto> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        log.warn("[{}] Constraint violations on {}: {}", correlationId, request.getRequestURI(), errors);

        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/constraint-violation")
                .title("Constraint Violation")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("One or more constraint rules were violated.")
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetailsDto> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        String detail = "Malformed JSON request body. Please verify the JSON syntax and field data types.";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String fieldPath = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .reduce((a, b) -> a + "." + b)
                    .orElse("unknown");
            detail = String.format("Invalid value '%s' for field '%s'. Expected format: %s.",
                    ife.getValue(), fieldPath, ife.getTargetType().getSimpleName());
        } else if (cause != null && cause.getMessage() != null) {
            detail = "Malformed JSON payload: " + cause.getMessage().split("\n")[0];
        }

        log.warn("[{}] HttpMessageNotReadable on {}: {}", correlationId, request.getRequestURI(), detail);

        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/malformed-json")
                .title("Malformed Request Payload")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetailsDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid type";
        String detail = String.format("Parameter '%s' with value '%s' is invalid. Expected type: %s.",
                ex.getName(), ex.getValue(), requiredType);

        log.warn("[{}] Type mismatch on {}: {}", correlationId, request.getRequestURI(), detail);

        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/type-mismatch")
                .title("Invalid Parameter Type")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetailsDto> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        String detail = String.format("HTTP method '%s' is not supported for endpoint '%s'. Supported methods: %s.",
                ex.getMethod(), request.getRequestURI(), ex.getSupportedHttpMethods());

        log.warn("[{}] Method not supported on {}: {}", correlationId, request.getRequestURI(), detail);

        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/method-not-allowed")
                .title("Method Not Allowed")
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(problem);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetailsDto> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        log.warn("[{}] Resource not found on {}: {}", correlationId, request.getRequestURI(), ex.getMessage());

        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/not-found")
                .title("Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(String.format("The requested resource '%s' was not found on this server.", request.getRequestURI()))
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetailsDto> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        log.warn("[{}] Access denied on {}: {}", correlationId, request.getRequestURI(), ex.getMessage());

        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/forbidden")
                .title("Forbidden")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("Access denied: You do not possess the required permissions or roles to perform this action.")
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetailsDto> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        log.warn("[{}] Authentication failure on {}: {}", correlationId, request.getRequestURI(), ex.getMessage());

        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/unauthorized")
                .title("Unauthorized")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail("Authentication required: Please provide a valid Bearer token.")
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetailsDto> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/" + status.value())
                .title(status.getReasonPhrase())
                .status(status.value())
                .detail(ex.getReason() != null ? ex.getReason() : status.getReasonPhrase())
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailsDto> handleGenericException(Exception ex, HttpServletRequest request) {
        String correlationId = getCorrelationId();
        log.error("[{}] Unhandled Internal Server Error on {}:", correlationId, request.getRequestURI(), ex);

        ProblemDetailsDto problem = ProblemDetailsDto.builder()
                .type("https://e-comm.local/errors/internal-server-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(String.format("An unexpected internal error occurred. Please quote correlation ID '%s' when contacting support.", correlationId))
                .instance(request.getRequestURI())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private String getCorrelationId() {
        String id = MDC.get(MDC_CORRELATION_KEY);
        return (id != null && !id.isBlank()) ? id : "N/A";
    }
}
