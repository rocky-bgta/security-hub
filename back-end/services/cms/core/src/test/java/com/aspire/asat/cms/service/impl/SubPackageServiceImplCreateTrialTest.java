package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.product.ProductWithSinglePackageResponse;
import com.aspire.asat.cms.dto.subPackage.SubPackageResponseDto;
import com.aspire.asat.cms.dto.subPackage.TrialSubPackageCreationRequestDto;
import com.aspire.asat.cms.dto.topic.TopicMinimalDto;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.repository.ExamRepository;
import com.aspire.asat.cms.repository.ProductPackageRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.custom.SubPackageRepositoryCustom;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.service.ProductService;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.cms.service.topic.TopicService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubPackageServiceImplCreateTrialTest {

    @Mock private SubPackageRepository subPackageRepository;
    @Mock private SubPackageRepositoryCustom subPackageRepositoryCustom;
    @Mock private ProductRepository productRepository;
    @Mock private ProductPackageRepository productPackageRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private UserSubPackageRepository userSubPackageRepository;
    @Mock private ExamRepository examRepository;
    @Mock private TopicService topicService;
    @Mock private ProductService productService;
    @Mock private UserCurrentContextService userCurrentContextService;
    @Mock private RegistrationServiceClient registrationServiceClient;

    private SubPackageServiceImpl subPackageService;

    @BeforeEach
    void setUp() {
        subPackageService = new SubPackageServiceImpl(
                subPackageRepository,
                subPackageRepositoryCustom,
                productRepository,
                productPackageRepository,
                topicRepository,
                userSubPackageRepository,
                examRepository,
                topicService,
                productService,
                userCurrentContextService,
                registrationServiceClient);
    }

    @Test
    void createTrialSubPackage_UsesProductSpecificName() {
        stubTopics();
        when(productService.getProductPackageDetails("phish-id", "phish-pkg"))
                .thenReturn(ProductWithSinglePackageResponse.builder()
                        .productId("phish-id")
                        .productName("Phishing Simulation")
                        .build());
        when(subPackageRepository.existsByNameAndClientId("Phishing Simulation Trial", "client-1"))
                .thenReturn(false);
        when(subPackageRepository.save(any(SubPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findById("phish-id")).thenReturn(Optional.of(Product.builder()
                .id("phish-id")
                .productName("Phishing Simulation")
                .build()));
        when(userSubPackageRepository.countBySubPackageId(anyString())).thenReturn(0L);

        SubPackageResponseDto response = subPackageService.createTrialSubPackage(TrialSubPackageCreationRequestDto.builder()
                .productId("phish-id")
                .packageId("phish-pkg")
                .productPackageId("client-product-1")
                .clientAdminId("client-1")
                .trialPeriodDays(30)
                .build());

        ArgumentCaptor<SubPackage> captor = ArgumentCaptor.forClass(SubPackage.class);
        verify(subPackageRepository).save(captor.capture());
        assertEquals("Phishing Simulation Trial", captor.getValue().getName());
        assertEquals("Phishing Simulation Trial", response.getName());
    }

    @Test
    void createTrialSubPackage_SatKeepsLegacyASatTrialName() {
        stubTopics();
        when(productService.getProductPackageDetails("sat-id", "sat-pkg"))
                .thenReturn(ProductWithSinglePackageResponse.builder()
                        .productId("sat-id")
                        .productName("Security Awareness Training")
                        .build());
        when(subPackageRepository.existsByNameAndClientId("A-SAT Trial", "client-1")).thenReturn(false);
        when(subPackageRepository.save(any(SubPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findById("sat-id")).thenReturn(Optional.of(Product.builder()
                .id("sat-id")
                .productName("Security Awareness Training")
                .build()));
        when(userSubPackageRepository.countBySubPackageId(anyString())).thenReturn(0L);

        subPackageService.createTrialSubPackage(TrialSubPackageCreationRequestDto.builder()
                .productId("sat-id")
                .packageId("sat-pkg")
                .productPackageId("client-product-1")
                .clientAdminId("client-1")
                .trialPeriodDays(30)
                .build());

        ArgumentCaptor<SubPackage> captor = ArgumentCaptor.forClass(SubPackage.class);
        verify(subPackageRepository).save(captor.capture());
        assertEquals("A-SAT Trial", captor.getValue().getName());
    }

    @Test
    void createTrialSubPackage_SameNameReturnsExisting() {
        stubTopics();
        when(productService.getProductPackageDetails("sat-id", "sat-pkg"))
                .thenReturn(ProductWithSinglePackageResponse.builder()
                        .productId("sat-id")
                        .productName("Security Awareness Training")
                        .build());
        when(subPackageRepository.existsByNameAndClientId("A-SAT Trial", "client-1"))
                .thenReturn(true);
        SubPackage existing = SubPackage.builder()
                .id("existing-sp")
                .name("A-SAT Trial")
                .productId("sat-id")
                .packageId("sat-pkg")
                .clientId("client-1")
                .clientAdminId("client-1")
                .build();
        when(subPackageRepository.findByNameAndClientId("A-SAT Trial", "client-1"))
                .thenReturn(Optional.of(existing));
        when(productRepository.findById("sat-id")).thenReturn(Optional.of(Product.builder()
                .id("sat-id")
                .productName("Security Awareness Training")
                .build()));
        when(userSubPackageRepository.countBySubPackageId("existing-sp")).thenReturn(0L);

        SubPackageResponseDto response = subPackageService.createTrialSubPackage(TrialSubPackageCreationRequestDto.builder()
                .productId("sat-id")
                .packageId("sat-pkg")
                .productPackageId("client-product-1")
                .clientAdminId("client-1")
                .trialPeriodDays(30)
                .build());

        assertEquals("existing-sp", response.getId());
        assertEquals("A-SAT Trial", response.getName());
    }

    private void stubTopics() {
        when(topicService.getTopicsByProductAndPackage(anyString(), anyString(), isNull(), eq(0), eq(1000), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of(
                        TopicMinimalDto.builder().topicId("t1").build(),
                        TopicMinimalDto.builder().topicId("t2").build())));
    }
}
