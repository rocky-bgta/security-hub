package com.aspire.asat.registration.service.support;

import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.data.cms.response.CmsFullProductResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsProductResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationProductResolverTest {

    @Mock
    private CmsServiceClient cmsServiceClient;

    private SimulationProductResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SimulationProductResolver(
                cmsServiceClient,
                List.of("Phishing Simulation", "Smishing Simulation", "Vishing Simulation"));
    }

    @Test
    void isSimulationProduct_matchesByName() {
        when(cmsServiceClient.fetchAndCacheAllProducts()).thenReturn(Map.of(
                "phish-id", product("phish-id", "Phishing Simulation"),
                "sat-id", product("sat-id", "Security Awareness Training")));

        assertTrue(resolver.isSimulationProduct("phish-id"));
        assertFalse(resolver.isSimulationProduct("sat-id"));
    }

    @Test
    void isSimulationProduct_caseInsensitiveNameMatch() {
        when(cmsServiceClient.fetchAndCacheAllProducts()).thenReturn(Map.of(
                "smish-id", product("smish-id", "smishing simulation")));

        assertTrue(resolver.isSimulationProduct("smish-id"));
    }

    @Test
    void getSimulationProductIds_returnsOnlyConfiguredNames() {
        when(cmsServiceClient.fetchAndCacheAllProducts()).thenReturn(Map.of(
                "phish-id", product("phish-id", "Phishing Simulation"),
                "vish-id", product("vish-id", "Vishing Simulation"),
                "sat-id", product("sat-id", "Security Awareness Training")));

        Set<String> ids = resolver.getSimulationProductIds();

        assertEquals(Set.of("phish-id", "vish-id"), ids);
    }

    @Test
    void isSimulationProduct_blank_returnsFalse() {
        assertFalse(resolver.isSimulationProduct(null));
        assertFalse(resolver.isSimulationProduct("  "));
    }

    @Test
    void isSimulationProduct_fallsBackToSingleProductFetch() {
        when(cmsServiceClient.fetchAndCacheAllProducts()).thenReturn(Map.of());
        when(cmsServiceClient.getProductWithPackages("phish-id"))
                .thenReturn(CmsProductResponseDto.builder()
                        .productId("phish-id")
                        .productName("Phishing Simulation")
                        .build());

        assertTrue(resolver.isSimulationProduct("phish-id"));
    }

    private static CmsFullProductResponseDto product(String id, String name) {
        return CmsFullProductResponseDto.builder()
                .productId(id)
                .productName(name)
                .build();
    }
}
