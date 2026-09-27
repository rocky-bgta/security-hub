import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.common.message.ToastMessageResolver;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.common.message.ToastMessageResolver;
import com.aspire.asat.registration.exception.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ToastMessageResolver toastMessageResolver;

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler(toastMessageResolver);
        when(toastMessageResolver.resolve(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        globalExceptionHandler = new GlobalExceptionHandler(toastMessageResolver);
    }

    @Test
    void handleAspireException_returnsInternalServerError() {
        AspireException exception = new AspireException("Aspire error occurred");

        ResponseEntity<ApiResponseDto<Void>> response = globalExceptionHandler.handleAspireException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Aspire error occurred", response.getBody().getMessage());
    }

    @Test
    void handleRegistrationServiceException_returnsBadRequest() {
        RegistrationServiceException exception = new RegistrationServiceException("Registration service error occurred");

        ResponseEntity<ApiResponseDto<Void>> response = globalExceptionHandler.handleRegistrationException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Registration service error occurred", response.getBody().getMessage());
    }
}
