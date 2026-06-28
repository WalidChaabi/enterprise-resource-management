package com.company.erm.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should map business exceptions to their configured HTTP status")
    void handleBusinessException_shouldUseConfiguredStatus() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleBusinessException(
            new BusinessException("AUTH_ERROR", "Invalid email or password", HttpStatus.UNAUTHORIZED)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().code()).isEqualTo("AUTH_ERROR");
        assertThat(response.getBody().details()).containsExactly("Invalid email or password");
    }

    @Test
    @DisplayName("Should default business exceptions to 400 when status is missing")
    void handleBusinessException_shouldDefaultToBadRequest() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleBusinessException(
            new BusinessException("VALIDATION_FAILED", "Invalid input")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("Should map access denied exceptions to 403")
    void handleAccessDenied_shouldReturnForbidden() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleAccessDenied(
            new AccessDeniedException("User does not have required role")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().code()).isEqualTo("ACCESS_DENIED");
        assertThat(response.getBody().details()).containsExactly("User does not have required role");
    }

    @Test
    @DisplayName("Should map authentication exceptions to 401")
    void handleAuthentication_shouldReturnUnauthorized() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleAuthentication(
            new AuthenticationCredentialsNotFoundException("Authentication required")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().code()).isEqualTo("UNAUTHORIZED");
        assertThat(response.getBody().details()).containsExactly("Authentication required");
    }

    @Test
    @DisplayName("Should map generic authentication failures to 401")
    void handleAuthentication_shouldHandleBadCredentials() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleAuthentication(
            new BadCredentialsException("Bad credentials")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().code()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("Should map validation errors from request body binding to 400")
    void handleValidationErrors_shouldReturnBadRequestForMethodArgumentNotValid() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        var bindingResult = mock(org.springframework.validation.BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
            List.of(new FieldError("authRequest", "email", "must not be blank"))
        );

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleValidationErrors(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().details()).containsExactly("email: must not be blank");
    }

    @Test
    @DisplayName("Should map bind exceptions to 400")
    void handleValidationErrors_shouldReturnBadRequestForBindException() throws Exception {
        BindException exception = new BindException(new Object(), "employee");
        exception.addError(new FieldError("employee", "firstName", "must not be blank"));

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleValidationErrors(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().details()).containsExactly("firstName: must not be blank");
    }

    @Test
    @DisplayName("Should map constraint violations to 400")
    void handleConstraintViolation_shouldReturnBadRequest() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("createProject.name");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleConstraintViolation(
            new ConstraintViolationException(Set.of(violation))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().details()).containsExactly("createProject.name: must not be blank");
    }

    @Test
    @DisplayName("Should map illegal argument exceptions to 400")
    void handleIllegalArgument_shouldReturnBadRequest() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleIllegalArgument(
            new IllegalArgumentException("Invalid UUID format")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().details()).containsExactly("Invalid UUID format");
    }

    @Test
    @DisplayName("Should map unexpected exceptions to 500")
    void handleUnexpected_shouldReturnInternalServerError() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleUnexpected(
            new IllegalStateException("boom")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("UNEXPECTED_ERROR");
        assertThat(response.getBody().details()).containsExactly("Une erreur interne est survenue.");
    }
}
