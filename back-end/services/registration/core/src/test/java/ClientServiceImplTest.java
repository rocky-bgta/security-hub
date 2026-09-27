import com.aspire.asat.registration.exception.ClientDoesNotExistException;
import com.aspire.asat.registration.repository.ClientRepository;
import com.aspire.asat.registration.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getClientById_ShouldThrowClientDoesNotExistException() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ClientDoesNotExistException.class, () -> clientService.getClientById(clientId));
        verify(clientRepository, times(1)).findById(clientId);
    }

}