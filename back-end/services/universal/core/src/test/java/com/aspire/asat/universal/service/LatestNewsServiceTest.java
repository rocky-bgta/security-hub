package com.aspire.asat.universal.service;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.enums.Status;
import com.aspire.asat.universal.news.LatestNewsRequest;
import com.aspire.asat.universal.news.NewsSequenceRequest;
import com.aspire.asat.universal.repository.CategoryRepository;
import com.aspire.asat.universal.repository.LatestNewsRepository;
import com.aspire.asat.universal.repository.NewsLikeRepository;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LatestNewsServiceTest {

    @Mock
    private LatestNewsRepository latestNewsRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private NewsLikeRepository newsLikeRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private NewsCommentService newsCommentService;

    @InjectMocks
    private LatestNewsService latestNewsService;

    private CurrentUserContext mspContext;

    @BeforeEach
    void setUp() {
        mspContext = CurrentUserContext.builder()
                .userId("msp-user-id")
                .userType("MSP")
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext);
    }

    @Test
    void createNews_whenMspUser_throwsException() {
        LatestNewsRequest request = new LatestNewsRequest();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> latestNewsService.createNews(request)
        );

        assertEquals("Latest news cannot be created by MSP", exception.getMessage());
        verify(latestNewsRepository, never()).save(any());
    }

    @Test
    void updateNews_whenMspUser_throwsException() {
        LatestNewsRequest request = new LatestNewsRequest();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> latestNewsService.updateNews("news-id", request)
        );

        assertEquals("Latest news cannot be updated by MSP", exception.getMessage());
        verify(latestNewsRepository, never()).save(any());
    }

    @Test
    void deleteNews_whenMspUser_throwsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> latestNewsService.deleteNews("news-id")
        );

        assertEquals("Latest news cannot be deleted by MSP", exception.getMessage());
        verify(latestNewsRepository, never()).deleteById(anyString());
    }

    @Test
    void updateNewsSequence_whenMspUser_throwsException() {
        NewsSequenceRequest request = new NewsSequenceRequest("news-id", 1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> latestNewsService.updateNewsSequence(request)
        );

        assertEquals("Latest news cannot be modified by MSP", exception.getMessage());
        verify(latestNewsRepository, never()).save(any());
    }

    @Test
    void updateNewsStatus_whenMspUser_throwsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> latestNewsService.updateNewsStatus("news-id", Status.ACTIVE)
        );

        assertEquals("Latest news cannot be modified by MSP", exception.getMessage());
        verify(latestNewsRepository, never()).save(any());
    }
}
