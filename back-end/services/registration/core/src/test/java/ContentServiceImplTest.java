import com.aspire.asat.registration.data.ContentDto;
import com.aspire.asat.registration.exception.InvalidContentNameException;
import com.aspire.asat.registration.exception.InvalidContentTypeException;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.model.Content;
import com.aspire.asat.registration.repository.ContentRepository;
import com.aspire.asat.registration.service.impl.ContentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;

import static com.aspire.asat.registration.data.enums.ContentType.Video;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContentServiceImplTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ContentServiceImpl contentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_ShouldValidateAndSaveContent_WhenContentDtoIsValid() {

        ContentDto contentDto = new ContentDto();
        contentDto.setName("Valid Name");
        contentDto.setType(Video);

        Content content = new Content();
        when(contentRepository.save(any(Content.class))).thenReturn(content);

        ContentDto result = contentService.save(contentDto);

        assertNotNull(result);
        verify(contentRepository, times(1)).save(any(Content.class));

    }

    @Test
    void save_ShouldThrowException_WhenContentDtoIsInvalid() {

        ContentDto contentDto = new ContentDto();

        RegistationValidationException exception = assertThrows(RegistationValidationException.class, () -> {
            contentService.save(contentDto);
        });

        assertTrue(exception.getAllValidationException().stream()
                .anyMatch(e -> e instanceof InvalidContentNameException));
        assertTrue(exception.getAllValidationException().stream()
                .anyMatch(e -> e instanceof InvalidContentTypeException));

        verify(contentRepository, never()).save(any(Content.class));
    }

    @Test
    void validateContentDto_ShouldThrowValidationException_WhenNameOrTypeIsNull() {

        ContentDto contentDto = new ContentDto();

        RegistationValidationException exception = assertThrows(RegistationValidationException.class, () -> {
            contentService.validateContentDto(contentDto);
        });

        assertTrue(exception.getAllValidationException().stream()
                .anyMatch(e -> e instanceof InvalidContentNameException));
        assertTrue(exception.getAllValidationException().stream()
                .anyMatch(e -> e instanceof InvalidContentTypeException));

    }

    @Test
    void validateContentDto_ShouldNotThrowException_WhenNameAndTypeAreValid() {

        ContentDto contentDto = new ContentDto();
        contentDto.setName("Valid Name");
        contentDto.setType(Video);

        assertDoesNotThrow(() -> contentService.validateContentDto(contentDto));

    }

}
