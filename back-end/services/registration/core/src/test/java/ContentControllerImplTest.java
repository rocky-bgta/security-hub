import com.aspire.asat.registration.controller.impl.ContentControllerImpl;
import com.aspire.asat.registration.data.ContentDto;
import com.aspire.asat.registration.service.ContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContentControllerImplTest {

    @Mock
    private ContentService contentService;

    @InjectMocks
    private ContentControllerImpl contentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createContent_ShouldReturnCreatedContent() {

        ContentDto contentDto = new ContentDto();
        when(contentService.save(any(ContentDto.class))).thenReturn(contentDto);

        ResponseEntity<ContentDto> response = contentController.createContent(contentDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(contentDto, response.getBody());
        verify(contentService, times(1)).save(any(ContentDto.class));
    }

}
