package com.aspire.asat.phishing.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingCompletionStatusResolverTest {

    @ParameterizedTest
    @ValueSource(strings = {"complete", "COMPLETE", "COMPLETED", "PHISHING_TRAINING_COMPLETED"})
    void isCompleted_acceptsAllCmsCompletionVariants(String status) {
        assertTrue(TrainingCompletionStatusResolver.isCompleted(status));
    }

    @ParameterizedTest
    @ValueSource(strings = {"pending", "InProgress", "EXAM", "expired", "NOT_STARTED"})
    void isCompleted_rejectsNonCompletionStatuses(String status) {
        assertFalse(TrainingCompletionStatusResolver.isCompleted(status));
    }

    @Test
    void isCompleted_rejectsNullAndBlank() {
        assertFalse(TrainingCompletionStatusResolver.isCompleted(null));
        assertFalse(TrainingCompletionStatusResolver.isCompleted(""));
        assertFalse(TrainingCompletionStatusResolver.isCompleted("   "));
    }
}
