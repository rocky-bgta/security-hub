package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.dto.request.LandingPageUpdateRequest;
import com.aspire.asat.phishing.dto.response.DataCaptureTypeDto;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.LandingPageCategoryDto;
import com.aspire.asat.phishing.dto.response.LandingPageDto;
import com.aspire.asat.phishing.model.LandingPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LandingPageMapperTest {

    private final LandingPageMapper mapper = new LandingPageMapper();

    @Test
    void toListItemDto_omitsHtmlContentFromPreview() {
        LandingPage page = LandingPage.builder()
                .id("page-1")
                .htmlContent("<html>secret</html>")
                .build();

        LandingPageDto dto = mapper.toListItemDto(page, true, false);

        assertEquals("", dto.getLandingPageBodyPreview());
    }

    @Test
    void toDto_includesFullHtmlContentInPreview() {
        LandingPage page = LandingPage.builder()
                .id("page-1")
                .htmlContent("<html>secret</html>")
                .build();

        LandingPageDto dto = mapper.toDto(page, true, false);

        assertEquals("<html>secret</html>", dto.getLandingPageBodyPreview());
    }

    @Test
    void toDto_mapsEmbeddedCategoryAndDifficulty() {
        LandingPageCategoryDto category = LandingPageCategoryDto.builder()
                .id("cat-1")
                .name("Business")
                .build();
        DifficultyDto difficulty = DifficultyDto.builder()
                .id("diff-1")
                .name("Intermediate")
                .build();
        DataCaptureTypeDto dataCaptureType = DataCaptureTypeDto.builder()
                .id("dct-1")
                .name("Credentials")
                .build();

        LandingPage page = LandingPage.builder()
                .id("page-1")
                .clientId("client-1")
                .name("Test")
                .pageType(LandingPageType.CUSTOM)
                .category(category)
                .difficultyLevel(difficulty)
                .dataCaptureType(dataCaptureType)
                .htmlContent("<html></html>")
                .build();

        LandingPageDto dto = mapper.toDto(page, true, false);

        assertNotNull(dto.getCategory());
        assertEquals("cat-1", dto.getCategory().getId());
        assertNotNull(dto.getDifficultyLevel());
        assertEquals("diff-1", dto.getDifficultyLevel().getId());
        assertNotNull(dto.getDataCaptureType());
        assertEquals("dct-1", dto.getDataCaptureType().getId());
        assertEquals("Credentials", dto.getDataCaptureType().getName());
    }

    @Test
    void updateFromRequest_setsHtmlContentForLandingPageType() {
        LandingPage page = LandingPage.builder()
                .pageType(LandingPageType.LANDING_PAGE)
                .htmlContent("<html>old</html>")
                .build();

        mapper.updateFromRequest(page, LandingPageUpdateRequest.builder()
                .name("Updated")
                .htmlContent("<html>new</html>")
                .build());

        assertEquals("<html>new</html>", page.getHtmlContent());
    }

    @Test
    void updateFromRequest_leavesHtmlContentWhenRequestFieldIsNull() {
        LandingPage page = LandingPage.builder()
                .pageType(LandingPageType.LANDING_PAGE)
                .htmlContent("<html>unchanged</html>")
                .build();

        mapper.updateFromRequest(page, LandingPageUpdateRequest.builder()
                .name("Updated")
                .build());

        assertEquals("<html>unchanged</html>", page.getHtmlContent());
    }

    @Test
    void updateFromRequest_setsEmbeddedSnapshots() {
        LandingPage page = LandingPage.builder().build();
        LandingPageCategoryDto category = LandingPageCategoryDto.builder().id("cat-2").name("Social").build();
        DifficultyDto difficulty = DifficultyDto.builder().id("diff-2").name("Advanced").build();
        DataCaptureTypeDto dataCaptureType = DataCaptureTypeDto.builder().id("dct-2").name("Form Data").build();

        LandingPageUpdateRequest request = LandingPageUpdateRequest.builder()
                .name("Updated")
                .category(category)
                .difficultyLevel(difficulty)
                .dataCaptureType(dataCaptureType)
                .build();

        mapper.updateFromRequest(page, request);

        assertEquals("cat-2", page.getCategory().getId());
        assertEquals("diff-2", page.getDifficultyLevel().getId());
        assertEquals("dct-2", page.getDataCaptureType().getId());
    }
}
