package com.aspire.asat.cms.service.impl.topic;

import com.aspire.asat.cms.client.service.CountryServiceClient;
import com.aspire.asat.cms.config.TopicStatusConfig;
import com.aspire.asat.cms.mapper.CountryMapper;
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
import com.aspire.asat.common.service.files.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicServiceImplExistsByTopicNameTest {

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
    void existsByTopicName_whenNameExists_returnsTrue() {
        when(topicRepository.existsByTopicNameIgnoreCase("My Topic")).thenReturn(true);

        assertTrue(topicService.existsByTopicName("  My Topic  "));
        verify(topicRepository).existsByTopicNameIgnoreCase("My Topic");
    }

    @Test
    void existsByTopicName_whenNameDoesNotExist_returnsFalse() {
        when(topicRepository.existsByTopicNameIgnoreCase("Fresh Topic")).thenReturn(false);

        assertFalse(topicService.existsByTopicName("Fresh Topic"));
        verify(topicRepository).existsByTopicNameIgnoreCase("Fresh Topic");
    }

    @Test
    void existsByTopicName_whenBlank_returnsFalseWithoutRepositoryCall() {
        assertFalse(topicService.existsByTopicName("   "));
        assertFalse(topicService.existsByTopicName(null));
        verify(topicRepository, never()).existsByTopicNameIgnoreCase(org.mockito.ArgumentMatchers.anyString());
    }
}
