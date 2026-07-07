package com.company.erm.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleOptimisticLock_returns409WithClearMessage() {
        var response = handler.handleOptimisticLock(new OptimisticLockingFailureException("conflict"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("OPTIMISTIC_LOCK_CONFLICT");
        assertThat(response.getBody().details()).containsExactly(
                "The record was modified by another request. Reload and try again.");
    }
}
