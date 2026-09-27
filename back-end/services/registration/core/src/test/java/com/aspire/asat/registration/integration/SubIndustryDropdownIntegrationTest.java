package com.aspire.asat.registration.integration;

import com.aspire.asat.registration.data.dropdown.IndustryRequestDto;
import com.aspire.asat.registration.data.dropdown.IndustryRespDto;
import com.aspire.asat.registration.data.dropdown.SubIndustryRequestDto;
import com.aspire.asat.registration.data.dropdown.SubIndustryRespDto;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.model.dropdown.Industry;
import com.aspire.asat.registration.model.dropdown.SubIndustry;
import com.aspire.asat.registration.model.metadata.OrganizationType;
import com.aspire.asat.registration.repository.dropdown.IndustryRepository;
import com.aspire.asat.registration.repository.dropdown.SubIndustryRepository;
import com.aspire.asat.registration.repository.metadata.OrganizationTypeRepository;
import com.aspire.asat.registration.service.dropdown.impl.IndustryServiceImpl;
import com.aspire.asat.registration.service.dropdown.impl.SubIndustryServiceImpl;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end Industry/SubIndustry CRUD and filtering against a real MongoDB (Testcontainers).
 */
@Testcontainers(disabledWithoutDocker = true)
class SubIndustryDropdownIntegrationTest {

    private static final String ORG_TYPE_A = "org-type-a";
    private static final String ORG_TYPE_B = "org-type-b";
    private static final String INDUSTRY_A = "industry-a";
    private static final String INDUSTRY_B = "industry-b";

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    private MongoTemplate mongoTemplate;
    private OrganizationTypeRepository organizationTypeRepository;
    private IndustryRepository industryRepository;
    private SubIndustryRepository subIndustryRepository;
    private IndustryServiceImpl industryService;
    private SubIndustryServiceImpl subIndustryService;

    @BeforeEach
    void setUp() {
        mongoTemplate = new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(
                        MongoClients.create(mongoDBContainer.getConnectionString()),
                        "sub_industry_dropdown_it"));
        MongoRepositoryFactory factory = new MongoRepositoryFactory(mongoTemplate);
        organizationTypeRepository = factory.getRepository(OrganizationTypeRepository.class);
        industryRepository = factory.getRepository(IndustryRepository.class);
        subIndustryRepository = factory.getRepository(SubIndustryRepository.class);
        industryService = new IndustryServiceImpl(industryRepository, organizationTypeRepository);
        subIndustryService = new SubIndustryServiceImpl(
                subIndustryRepository, industryRepository, organizationTypeRepository);

        Instant now = Instant.now();
        organizationTypeRepository.save(OrganizationType.builder()
                .id(ORG_TYPE_A)
                .name("Type A")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        organizationTypeRepository.save(OrganizationType.builder()
                .id(ORG_TYPE_B)
                .name("Type B")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        industryRepository.save(Industry.builder()
                .id(INDUSTRY_A)
                .organizationTypeId(ORG_TYPE_A)
                .code("IND_A")
                .name("Industry A")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
        industryRepository.save(Industry.builder()
                .id(INDUSTRY_B)
                .organizationTypeId(ORG_TYPE_B)
                .code("IND_B")
                .name("Industry B")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.getDb().drop();
    }

    @Test
    void createIndustry_storesOrganizationTypeId() {
        IndustryRespDto created = industryService.createIndustry(IndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_A)
                .code("FIN")
                .name("Finance")
                .active(true)
                .build());

        assertEquals(ORG_TYPE_A, created.getOrganizationTypeId());
        assertEquals("FIN", created.getCode());
    }

    @Test
    void getActiveIndustries_filtersByOrganizationTypeId() {
        List<IndustryRespDto> forA = industryService.getActiveIndustries(ORG_TYPE_A);
        List<IndustryRespDto> forB = industryService.getActiveIndustries(ORG_TYPE_B);
        List<IndustryRespDto> all = industryService.getActiveIndustries(null);

        assertEquals(1, forA.size());
        assertEquals(ORG_TYPE_A, forA.get(0).getOrganizationTypeId());
        assertEquals(1, forB.size());
        assertEquals(ORG_TYPE_B, forB.get(0).getOrganizationTypeId());
        assertEquals(2, all.size());
    }

    @Test
    void createSubIndustry_storesOrganizationTypeAndIndustryId() {
        SubIndustryRespDto created = subIndustryService.createSubIndustry(SubIndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_A)
                .industryId(INDUSTRY_A)
                .code("FIN-01")
                .name("Financial Services")
                .active(true)
                .build());

        assertEquals(ORG_TYPE_A, created.getOrganizationTypeId());
        assertEquals(INDUSTRY_A, created.getIndustryId());
        assertEquals("FIN-01", created.getCode());

        SubIndustryRespDto fetched = subIndustryService.getSubIndustryById(created.getId());
        assertEquals(ORG_TYPE_A, fetched.getOrganizationTypeId());
        assertEquals(INDUSTRY_A, fetched.getIndustryId());
    }

    @Test
    void getSubIndustries_filtersByIndustryId() {
        seedSubIndustry(ORG_TYPE_A, INDUSTRY_A, "A1", "Alpha A");
        seedSubIndustry(ORG_TYPE_A, INDUSTRY_A, "A2", "Alpha B");
        seedSubIndustry(ORG_TYPE_B, INDUSTRY_B, "B1", "Beta A");

        List<SubIndustryRespDto> forA = subIndustryService.getSubIndustries(null, null, INDUSTRY_A);
        List<SubIndustryRespDto> forB = subIndustryService.getSubIndustries(null, null, INDUSTRY_B);
        List<SubIndustryRespDto> all = subIndustryService.getSubIndustries(null, null, null);

        assertEquals(2, forA.size());
        assertTrue(forA.stream().allMatch(s -> INDUSTRY_A.equals(s.getIndustryId())));
        assertEquals(1, forB.size());
        assertEquals(INDUSTRY_B, forB.get(0).getIndustryId());
        assertEquals(3, all.size());
    }

    @Test
    void getActiveSubIndustries_filtersByOrganizationTypeAndIndustry() {
        seedSubIndustry(ORG_TYPE_A, INDUSTRY_A, "A1", "Alpha A");
        seedSubIndustry(ORG_TYPE_A, INDUSTRY_A, "A2", "Alpha B");
        seedSubIndustry(ORG_TYPE_B, INDUSTRY_B, "B1", "Beta A");

        List<SubIndustryRespDto> byOrgType = subIndustryService.getActiveSubIndustries(ORG_TYPE_A, null);
        List<SubIndustryRespDto> byIndustry = subIndustryService.getActiveSubIndustries(null, INDUSTRY_B);
        List<SubIndustryRespDto> byBoth = subIndustryService.getActiveSubIndustries(ORG_TYPE_A, INDUSTRY_A);
        List<SubIndustryRespDto> all = subIndustryService.getActiveSubIndustries(null, null);

        assertEquals(2, byOrgType.size());
        assertTrue(byOrgType.stream().allMatch(s -> ORG_TYPE_A.equals(s.getOrganizationTypeId())));
        assertEquals(1, byIndustry.size());
        assertEquals(INDUSTRY_B, byIndustry.get(0).getIndustryId());
        assertEquals(2, byBoth.size());
        assertEquals(3, all.size());
    }

    @Test
    void getSubIndustries_combinesSearchAndIndustryId() {
        seedSubIndustry(ORG_TYPE_A, INDUSTRY_A, "FIN-01", "Financial Services");
        seedSubIndustry(ORG_TYPE_A, INDUSTRY_A, "RET-01", "Retail");
        seedSubIndustry(ORG_TYPE_B, INDUSTRY_B, "FIN-02", "Financial Advisors");

        List<SubIndustryRespDto> result = subIndustryService.getSubIndustries("fin", null, INDUSTRY_A);

        assertEquals(1, result.size());
        assertEquals("FIN-01", result.get(0).getCode());
        assertEquals(INDUSTRY_A, result.get(0).getIndustryId());
    }

    @Test
    void createSubIndustry_rejectsDuplicateCodeInSameIndustry() {
        seedSubIndustry(ORG_TYPE_A, INDUSTRY_A, "DUP", "Duplicate");

        assertThrows(DuplicateDataFoundException.class, () ->
                subIndustryService.createSubIndustry(SubIndustryRequestDto.builder()
                        .organizationTypeId(ORG_TYPE_A)
                        .industryId(INDUSTRY_A)
                        .code("DUP")
                        .name("Another")
                        .build()));
    }

    @Test
    void createSubIndustry_allowsSameCodeInDifferentIndustry() {
        seedSubIndustry(ORG_TYPE_A, INDUSTRY_A, "SHARED", "Shared Name A");

        SubIndustryRespDto created = subIndustryService.createSubIndustry(SubIndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_B)
                .industryId(INDUSTRY_B)
                .code("SHARED")
                .name("Shared Name B")
                .active(true)
                .build());

        assertEquals(INDUSTRY_B, created.getIndustryId());
        assertEquals(ORG_TYPE_B, created.getOrganizationTypeId());
        assertEquals("SHARED", created.getCode());
    }

    private void seedSubIndustry(String organizationTypeId, String industryId, String code, String name) {
        Instant now = Instant.now();
        subIndustryRepository.save(SubIndustry.builder()
                .id(UUID.randomUUID().toString())
                .organizationTypeId(organizationTypeId)
                .industryId(industryId)
                .code(code)
                .name(name)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }
}
