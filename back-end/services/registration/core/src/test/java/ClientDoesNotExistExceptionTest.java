import com.aspire.asat.registration.exception.ClientDoesNotExistException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientDoesNotExistExceptionTest {

    @Test
    void testDefaultConstructor() {

        ClientDoesNotExistException exception = new ClientDoesNotExistException();

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithMessage() {

        String errorMessage = "Client does not exist";

        ClientDoesNotExistException exception = new ClientDoesNotExistException(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());

    }

    @Test
    void testConstructorWithMessageAndCause() {

        String errorMessage = "Client does not exist";
        Throwable cause = new Throwable("Root cause");

        ClientDoesNotExistException exception = new ClientDoesNotExistException(errorMessage, cause);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());

    }
}