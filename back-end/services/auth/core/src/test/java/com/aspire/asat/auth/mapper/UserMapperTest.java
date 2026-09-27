package com.aspire.asat.auth.mapper;

import com.aspire.asat.auth.dto.PurchaseProductDto;
import com.aspire.asat.auth.dto.UserDetailsResponse;
import com.aspire.asat.auth.dto.enums.AdminStatus;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.ClientAdmin;
import com.aspire.asat.auth.repository.ClientAdminRepository;
import com.aspire.asat.auth.repository.RoleRepository;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.common.enums.UserType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientAdminRepository clientAdminRepository;

    @InjectMocks
    private UserMapper userMapper;

    @Test
    void mapToResponse_ClientAdmin_IncludesPurchaseProductsAndTags() throws Exception {
        UUID userId = UUID.randomUUID();
        AspireUser user = AspireUser.builder()
                .userId(userId)
                .username("admin@example.com")
                .email("admin@example.com")
                .firstName("Client")
                .lastName("Admin")
                .userType(UserType.CLIENT_ADMIN.getValue())
                .status("ACTIVE")
                .build();
        when(clientAdminRepository.findById(userId.toString())).thenReturn(Optional.of(
                ClientAdmin.builder().id(userId.toString()).status(AdminStatus.ACTIVE).timeZone("UTC").build()
        ));

        List<PurchaseProductDto> products = List.of(
                PurchaseProductDto.builder().productId("prod-1").productName("Phishing Awareness").build()
        );
        List<String> tags = List.of("phishing", "security");

        UserDetailsResponse response = userMapper.mapToResponse(user, false, tags, products);

        assertEquals(1, response.getPurchaseProducts().size());
        assertEquals("prod-1", response.getPurchaseProducts().get(0).getProductId());
        assertEquals("Phishing Awareness", response.getPurchaseProducts().get(0).getProductName());
        assertEquals(tags, response.getClientProductTags());
        assertEquals("UTC", response.getTimeZone());

        JsonNode json = new ObjectMapper().valueToTree(response);
        assertTrue(json.has("purchaseProducts"));
        assertTrue(json.has("clientProductTags"));
        assertEquals("prod-1", json.get("purchaseProducts").get(0).get("productId").asText());
        assertEquals("phishing", json.get("clientProductTags").get(0).asText());
        assertEquals("security", json.get("clientProductTags").get(1).asText());
    }

    @Test
    void mapToResponse_NonAdmin_LeavesPurchaseProductsAndTagsNull() {
        AspireUser user = AspireUser.builder()
                .userId(UUID.randomUUID())
                .username("user@example.com")
                .email("user@example.com")
                .userType(UserType.USER.getValue())
                .clientAdminId("admin-1")
                .status("ACTIVE")
                .build();
        when(clientAdminRepository.findById("admin-1")).thenReturn(Optional.empty());

        UserDetailsResponse response = userMapper.mapToResponse(user, null, null, null);

        assertNull(response.getPurchaseProducts());
        assertNull(response.getClientProductTags());
    }
}
