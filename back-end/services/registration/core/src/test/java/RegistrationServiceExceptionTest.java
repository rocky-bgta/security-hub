import com.aspire.asat.registration.exception.RegistrationServiceException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceExceptionTest {

    @Test
    void testDefaultConstructor() {

        RegistrationServiceException exception = new RegistrationServiceException();

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithMessage() {

        String errorMessage = "This is a test error message";

        RegistrationServiceException exception = new RegistrationServiceException(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());

    }

    @Test
    void testConstructorWithMessageAndCause() {

        String errorMessage = "This is a test error message";
        Throwable cause = new Throwable("Cause of the exception");

        RegistrationServiceException exception = new RegistrationServiceException(errorMessage, cause);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());

    }

}