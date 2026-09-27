package com.aspire.asat.cms.service.impl.topic;

import com.aspire.asat.cms.client.service.CountryServiceClient;
import com.aspire.asat.cms.config.TopicStatusConfig;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.topic.TopicRespDto;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.mapper.CountryMapper;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.ProductPackageRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.question.QuestionCustomRepository;
import com.aspire.asat.cms.repository.topic.CategoryRepository;
import com.aspire.asat.cms.repository.topic.ComplianceRepository;
import com.aspire.asat.cms.repository.topic.ContentTypeRepository;
import com.aspire.asat.cms.repository.topic.TopicDistributionRepositoryCustom;
import com.aspire.asat.cms.repository.topic.TopicFilterRepositoryCustom;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.repository.topic.TopicRepositoryCustom;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.service.files.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicServiceImplPrivateTopicsTest {

    private static final String CLIENT_ID = "client-admin-1";

    @Mock private TopicRepository topicRepository;
    @Mock private TopicFilterRepositoryCustom topicFilterRepositoryCustom;
    @Mock private TopicRepositoryCustom topicRepositoryCustom;
    @Mock private TopicDistributionRepositoryCustom topicDistributionRepositoryCustom;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CountryServiceClient countryServiceClient;
    @Mock private CountryMapper countryMapper;
    @Mock private ComplianceRepository complianceRepository;
    @Mock private ContentTypeRepository contentTypeRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductPackageRepository productPackageRepository;
    @Mock private QuestionCustomRepository questionCustomRepository;
    @Mock private FileService fileService;
    @Mock private TopicStatusConfig topicStatusConfig;
    @Mock private SubPackageRepository subPackageRepository;
    @Mock private MongoTemplate mongoTemplate;
    @Mock private UserCurrentContextService userCurrentContextService;
    @Mock private com.aspire.asat.cms.service.external.RegistrationServiceClient registrationServiceClient;

    @InjectMocks
    private TopicServiceImpl topicService;

    @Test
    void getPrivateTopicsForCurrentClient_missingClientAdminId_throwsUnauthorized() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("  ").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        assertThrows(ResourceNotFoundException.class,
                () -> topicService.getPrivateTopicsForCurrentClient(null, null, 0, 10, "createdAt", "desc"));
        verify(topicRepository, never()).findByIsPrivateTrueAndClientId(any(), any());
    }

    @Test
    void getPrivateTopicsForCurrentClient_returnsOnlyOwnedPrivateTopics() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId(CLIENT_ID).build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        Topic privateTopic = Topic.builder()
                .id("priv-1")
                .topicName("microContent of Acme")
                .status(TopicStatus.ENABLED)
                .clientId(CLIENT_ID)
                .isPrivate(true)
                .chapterIds(List.of("ch-1"))
                .build();
        when(topicRepository.findByIsPrivateTrueAndClientId(eq(CLIENT_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(privateTopic)));

        Page<TopicRespDto> page = topicService.getPrivateTopicsForCurrentClient(
                null, null, 0, 10, "createdAt", "desc");

        assertEquals(1, page.getContent().size());
        TopicRespDto dto = page.getContent().get(0);
        assertEquals("priv-1", dto.getId());
        assertEquals("microContent of Acme", dto.getTopicName());
        assertEquals(TopicStatus.ENABLED, dto.getStatus());
        assertEquals(List.of("ch-1"), dto.getChapterIds());
        assertEquals(CLIENT_ID, dto.getClientId());
        assertTrue(dto.getIsPrivate());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(topicRepository).findByIsPrivateTrueAndClientId(eq(CLIENT_ID), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getPrivateTopicsForCurrentClient_withStatusAndSearch_usesCombinedQuery() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId(CLIENT_ID).build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(topicRepository.findByIsPrivateTrueAndClientIdAndStatusAndTopicNameContainingIgnoreCase(
                eq(CLIENT_ID), eq(TopicStatus.ENABLED), eq("micro"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        topicService.getPrivateTopicsForCurrentClient("micro", TopicStatus.ENABLED, 0, 5, "topicName", "asc");

        verify(topicRepository).findByIsPrivateTrueAndClientIdAndStatusAndTopicNameContainingIgnoreCase(
                eq(CLIENT_ID), eq(TopicStatus.ENABLED), eq("micro"), any(Pageable.class));
        verify(topicRepository, never()).findByIsPrivateTrueAndClientId(any(), any());
    }
}
