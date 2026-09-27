package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.enums.MenuType;
import com.aspire.asat.registration.data.roles.MenuPermissionDto;
import com.aspire.asat.registration.data.roles.UserMenuPermissionResponse;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.model.RolePermission;
import com.aspire.asat.registration.model.menu.Menu;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.RolePermissionRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.repository.menus.MenuRepository;
import com.aspire.asat.registration.repository.menus.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceImplProductFilterTest {

    private static final String USERNAME = "admin@client.com";
    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String ROLE_ID = "role-client-admin";
    private static final String PRODUCT_A = "product-a";
    private static final String PRODUCT_B = "product-b";
    private static final String SHARED_MENU_ID = "menu-shared";
    private static final String PRODUCT_MENU_ID = "menu-product";
    private static final String OTHER_PRODUCT_MENU_ID = "menu-other-product";

    @Mock
    private RolePermissionRepository rolePermissionRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private AspireUserRepository aspireUserRepository;
    @Mock
    private ClientProductRepository clientProductRepository;

    @InjectMocks
    private RolePermissionServiceImpl rolePermissionService;

    @Test
    void isMenuAllowedForProducts_nullOrEmptyProductIds_alwaysAllowed() {
        Menu shared = menu(SHARED_MENU_ID, "SHARED", null);
        Menu empty = menu("empty", "EMPTY", List.of());

        assertTrue(rolePermissionService.isMenuAllowedForProducts(shared, Set.of()));
        assertTrue(rolePermissionService.isMenuAllowedForProducts(empty, Set.of(PRODUCT_A)));
    }

    @Test
    void isMenuAllowedForProducts_requiresIntersection() {
        Menu bound = menu(PRODUCT_MENU_ID, "PHISH", List.of(PRODUCT_A));

        assertTrue(rolePermissionService.isMenuAllowedForProducts(bound, Set.of(PRODUCT_A, PRODUCT_B)));
        assertFalse(rolePermissionService.isMenuAllowedForProducts(bound, Set.of(PRODUCT_B)));
        assertFalse(rolePermissionService.isMenuAllowedForProducts(bound, Set.of()));
    }

    @Test
    void getCurrentUserRolePermissions_filtersByPurchasedProducts() {
        AspireUser user = clientAdminUser();
        when(aspireUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        RolePermission rolePermission = RolePermission.builder()
                .roleId(ROLE_ID)
                .roleName("CLIENT_ADMIN")
                .menuPermissions(List.of(
                        menuPermission(SHARED_MENU_ID, "SHARED"),
                        menuPermission(PRODUCT_MENU_ID, "PHISH"),
                        menuPermission(OTHER_PRODUCT_MENU_ID, "OTHER")
                ))
                .build();
        when(rolePermissionRepository.findRolePermissionsByRoleIds(List.of(ROLE_ID)))
                .thenReturn(List.of(rolePermission));

        when(menuRepository.findById(SHARED_MENU_ID))
                .thenReturn(Optional.of(menu(SHARED_MENU_ID, "SHARED", null)));
        when(menuRepository.findById(PRODUCT_MENU_ID))
                .thenReturn(Optional.of(menu(PRODUCT_MENU_ID, "PHISH", List.of(PRODUCT_A))));
        when(menuRepository.findById(OTHER_PRODUCT_MENU_ID))
                .thenReturn(Optional.of(menu(OTHER_PRODUCT_MENU_ID, "OTHER", List.of(PRODUCT_B))));

        when(clientProductRepository.findByClientAdminIdAndLicenseStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(List.of(ClientProduct.builder()
                        .clientAdminId(CLIENT_ADMIN_ID)
                        .productId(PRODUCT_A)
                        .licenseStatus("ACTIVE")
                        .build()));

        CurrentUserContext context = CurrentUserContext.builder()
                .clientAdminId(CLIENT_ADMIN_ID)
                .userType(UserType.CLIENT_ADMIN.name())
                .build();

        List<UserMenuPermissionResponse> result =
                rolePermissionService.getCurrentUserRolePermissions(context, USERNAME);

        assertEquals(1, result.size());
        Set<String> menuIds = result.get(0).getUserMenuResponses().stream()
                .map(m -> m.getId())
                .collect(Collectors.toSet());
        assertEquals(Set.of(SHARED_MENU_ID, PRODUCT_MENU_ID), menuIds);
        assertFalse(menuIds.contains(OTHER_PRODUCT_MENU_ID));
    }

    @Test
    void getCurrentUserRolePermissions_excludesProductMenusWhenNoPurchases() {
        AspireUser user = clientAdminUser();
        when(aspireUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        RolePermission rolePermission = RolePermission.builder()
                .roleId(ROLE_ID)
                .roleName("CLIENT_ADMIN")
                .menuPermissions(List.of(
                        menuPermission(SHARED_MENU_ID, "SHARED"),
                        menuPermission(PRODUCT_MENU_ID, "PHISH")
                ))
                .build();
        when(rolePermissionRepository.findRolePermissionsByRoleIds(List.of(ROLE_ID)))
                .thenReturn(List.of(rolePermission));

        when(menuRepository.findById(SHARED_MENU_ID))
                .thenReturn(Optional.of(menu(SHARED_MENU_ID, "SHARED", List.of())));
        when(menuRepository.findById(PRODUCT_MENU_ID))
                .thenReturn(Optional.of(menu(PRODUCT_MENU_ID, "PHISH", List.of(PRODUCT_A))));

        when(clientProductRepository.findByClientAdminIdAndLicenseStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(List.of());

        CurrentUserContext context = CurrentUserContext.builder()
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();

        List<UserMenuPermissionResponse> result =
                rolePermissionService.getCurrentUserRolePermissions(context, USERNAME);

        Set<String> menuIds = result.get(0).getUserMenuResponses().stream()
                .map(m -> m.getId())
                .collect(Collectors.toSet());
        assertEquals(Set.of(SHARED_MENU_ID), menuIds);
    }

    @Test
    void getCurrentUserRolePermissions_usesAspireUserClientAdminIdWhenContextMissing() {
        AspireUser user = clientAdminUser();
        when(aspireUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        RolePermission rolePermission = RolePermission.builder()
                .roleId(ROLE_ID)
                .roleName("CLIENT_ADMIN")
                .menuPermissions(List.of(menuPermission(PRODUCT_MENU_ID, "PHISH")))
                .build();
        when(rolePermissionRepository.findRolePermissionsByRoleIds(List.of(ROLE_ID)))
                .thenReturn(List.of(rolePermission));
        when(menuRepository.findById(PRODUCT_MENU_ID))
                .thenReturn(Optional.of(menu(PRODUCT_MENU_ID, "PHISH", List.of(PRODUCT_A))));
        when(clientProductRepository.findByClientAdminIdAndLicenseStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(List.of(ClientProduct.builder().productId(PRODUCT_A).licenseStatus("ACTIVE").build()));

        List<UserMenuPermissionResponse> result =
                rolePermissionService.getCurrentUserRolePermissions(null, USERNAME);

        assertEquals(1, result.get(0).getUserMenuResponses().size());
        assertEquals(PRODUCT_MENU_ID, result.get(0).getUserMenuResponses().get(0).getId());
    }

    @Test
    void getCurrentUserRolePermissions_superAdmin_bypassesProductFilter() {
        AspireUser superAdmin = AspireUser.builder()
                .id(UUID.randomUUID())
                .username(USERNAME)
                .userType(UserType.SUPER_ADMIN.name())
                .roles(List.of(ROLE_ID))
                .build();
        when(aspireUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(superAdmin));

        Menu allMenu = menu(PRODUCT_MENU_ID, "PHISH", List.of(PRODUCT_A));
        when(menuRepository.findAll()).thenReturn(List.of(allMenu));
        when(permissionRepository.findByMenuCodeIn(List.of("PHISH"))).thenReturn(List.of());

        CurrentUserContext context = CurrentUserContext.builder()
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();

        List<UserMenuPermissionResponse> result =
                rolePermissionService.getCurrentUserRolePermissions(context, USERNAME);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserMenuResponses().size());
        verify(clientProductRepository, never())
                .findByClientAdminIdAndLicenseStatus(CLIENT_ADMIN_ID, "ACTIVE");
    }

    @Test
    void getCurrentUserRolePermissions_noClientAdminId_skipsProductFilter() {
        AspireUser mspUser = AspireUser.builder()
                .id(UUID.randomUUID())
                .username(USERNAME)
                .userType(UserType.MSP.name())
                .roles(List.of(ROLE_ID))
                .clientAdminId(null)
                .build();
        when(aspireUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(mspUser));

        RolePermission rolePermission = RolePermission.builder()
                .roleId(ROLE_ID)
                .roleName("MSP")
                .menuPermissions(List.of(menuPermission(PRODUCT_MENU_ID, "PHISH")))
                .build();
        when(rolePermissionRepository.findRolePermissionsByRoleIds(List.of(ROLE_ID)))
                .thenReturn(List.of(rolePermission));
        when(menuRepository.findById(PRODUCT_MENU_ID))
                .thenReturn(Optional.of(menu(PRODUCT_MENU_ID, "PHISH", List.of(PRODUCT_A))));

        List<UserMenuPermissionResponse> result =
                rolePermissionService.getCurrentUserRolePermissions(
                        CurrentUserContext.builder().userType(UserType.MSP.name()).build(),
                        USERNAME);

        assertEquals(1, result.get(0).getUserMenuResponses().size());
        verify(clientProductRepository, never())
                .findByClientAdminIdAndLicenseStatus(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString());
    }

    private AspireUser clientAdminUser() {
        return AspireUser.builder()
                .id(UUID.randomUUID())
                .username(USERNAME)
                .userType(UserType.CLIENT_ADMIN.name())
                .roles(List.of(ROLE_ID))
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();
    }

    private Menu menu(String id, String code, List<String> productIds) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setCode(code);
        menu.setName(code);
        menu.setUrl("/" + code.toLowerCase());
        menu.setIcon("icon");
        menu.setMenuType(MenuType.MAIN_MENU.getMenuType());
        menu.setSequenceNumber(1);
        menu.setActions(List.of("VIEW"));
        menu.setProductIds(productIds);
        return menu;
    }

    private MenuPermissionDto menuPermission(String menuId, String menuCode) {
        return MenuPermissionDto.builder()
                .menuId(menuId)
                .menuCode(menuCode)
                .permittedActions(List.of("VIEW"))
                .permissions(List.of(menuCode + ":VIEW"))
                .build();
    }
}
