import com.aspire.asat.registration.controller.impl.ClientControllerImpl;
import com.aspire.asat.registration.data.ClientDto;
import com.aspire.asat.registration.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientControllerImplTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientControllerImpl clientController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getAllClients_ShouldReturnListOfClients() {

        List<ClientDto> clients = Arrays.asList(new ClientDto(), new ClientDto());
        when(clientService.getAllClients(anyInt(), anyInt())).thenReturn(clients);

        ResponseEntity<List<ClientDto>> response = clientController.getAllClients(0, 5);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(clients, response.getBody());
        verify(clientService, times(1)).getAllClients(anyInt(), anyInt());
    }

    @Test
    void getClientById_ShouldReturnClientById() {

        UUID clientId = UUID.randomUUID();
        ClientDto clientDto = new ClientDto();
        when(clientService.getClientById(clientId)).thenReturn(clientDto);

        ResponseEntity<ClientDto> response = clientController.getClientById(clientId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(clientDto, response.getBody());
        verify(clientService, times(1)).getClientById(clientId);
    }

}