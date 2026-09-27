package com.aspire.asat.cms.controller.impl.topic;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.service.topic.TopicService;
import com.aspire.asat.cms.service.user_operations.ClientUserOperationService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TopicControllerImplDeleteAuthTest {

    private static final String TOPIC_ID = "topic-1";

    @Mock
    private TopicService topicService;
    @Mock
    private ClientUserOperationService clientUserOperationService;
    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private TopicControllerImpl controller;

    @Test
    void deleteTopicById_delegatesToService() {
        // Authorization for delete lives in TopicServiceImpl; controller is a thin delegate.
        ResponseEntity<ApiResponseDto<Void>> response = controller.deleteTopicById(TOPIC_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Topic deleted successfully", response.getBody().getMessage());
        verify(topicService).deleteTopicById(TOPIC_ID);
    }
}
