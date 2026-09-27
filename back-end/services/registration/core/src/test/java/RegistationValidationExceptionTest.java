import com.aspire.asat.common.exception.AspireValidationException;
import com.aspire.asat.registration.exception.RegistationValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistationValidationExceptionTest {

    private RegistationValidationException validationException;

    @BeforeEach
    void setUp() {
        validationException = new RegistationValidationException();
    }

    @Test
    void testDefaultConstructor() {

        RegistationValidationException exception = new RegistationValidationException();

        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertTrue(exception.getAllValidationException().isEmpty());
    }

    @Test
    void testConstructorWithMessage() {

        String errorMessage = "Validation failed";

        RegistationValidationException exception = new RegistationValidationException(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertTrue(exception.getAllValidationException().isEmpty());
    }

    @Test
    void testConstructorWithMessageAndCause() {

        String errorMessage = "Validation failed";
        Throwable cause = new Throwable("Root cause");

        RegistationValidationException exception = new RegistationValidationException(errorMessage, cause);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception.getAllValidationException().isEmpty());
    }

    @Test
    void testAddValidationException() {

        AspireValidationException mockValidationException = new MockValidationException("Field validation failed");

        validationException.addValidationException(mockValidationException);

        List<? extends AspireValidationException> validationExceptions = validationException.getAllValidationException();
        assertNotNull(validationExceptions);
        assertEquals(1, validationExceptions.size());
        assertEquals(mockValidationException, validationExceptions.get(0));
    }

    @Test
    void testGetAllValidationExceptions_ShouldReturnAllExceptions() {

        AspireValidationException validationException1 = new MockValidationException("Field1 validation failed");
        AspireValidationException validationException2 = new MockValidationException("Field2 validation failed");

        validationException.addValidationException(validationException1);
        validationException.addValidationException(validationException2);

        List<? extends AspireValidationException> validationExceptions = validationException.getAllValidationException();
        assertEquals(2, validationExceptions.size());
        assertTrue(validationExceptions.contains(validationException1));
        assertTrue(validationExceptions.contains(validationException2));
    }

    private static class MockValidationException extends Exception implements AspireValidationException {
        public MockValidationException(String message) {
            super(message);
        }

        @Override
        public List<? extends AspireValidationException> getAllValidationException() {
            return List.of();
        }
    }
}