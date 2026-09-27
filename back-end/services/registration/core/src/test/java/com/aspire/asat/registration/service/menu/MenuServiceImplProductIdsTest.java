package com.aspire.asat.registration.service.menu;

import com.aspire.asat.registration.data.enums.MenuStatus;
import com.aspire.asat.registration.data.enums.MenuType;
import com.aspire.asat.registration.data.menu.MenuRequestDto;
import com.aspire.asat.registration.data.menu.MenuResponseDto;
import com.aspire.asat.registration.data.menu.MenuWithPermissionsDto;
import com.aspire.asat.registration.model.menu.Menu;
import com.aspire.asat.registration.repository.menus.MenuRepository;
import com.aspire.asat.registration.repository.menus.PermissionRepository;
import com.aspire.asat.registration.utils.UniqueIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplProductIdsTest {

    private static final String MENU_ID = "menu-1";
    private static final String PRODUCT_A = "product-a";
    private static final String PRODUCT_B = "product-b";

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private MenuPermissionService menuPermissionService;
    @Mock
    private UniqueIdGenerator uniqueIdGenerator;

    @InjectMocks
    private MenuServiceImpl menuService;

    @BeforeEach
    void setUp() throws Exception {
        // ModelMapper is a concrete dependency; inject a real instance
        var field = MenuServiceImpl.class.getDeclaredField("modelMapper");
        field.setAccessible(true);
        field.set(menuService, new ModelMapper());
    }

    @Test
    void createMenu_persistsProductIds() {
        MenuRequestDto request = baseRequest();
        request.setProductIds(List.of(PRODUCT_A, PRODUCT_B));

        when(menuRepository.existsByCode("PHISH")).thenReturn(false);
        when(uniqueIdGenerator.generateUUID()).thenReturn(MENU_ID);
        when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> inv.getArgument(0));
        when(menuPermissionService.generatePermissions(any(Menu.class))).thenReturn(List.of());
        when(permissionRepository.saveAll(anyList())).thenReturn(List.of());

        MenuResponseDto response = menuService.createMenu(request);

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).save(captor.capture());
        assertEquals(List.of(PRODUCT_A, PRODUCT_B), captor.getValue().getProductIds());
        assertEquals(List.of(PRODUCT_A, PRODUCT_B), response.getProductIds());
    }

    @Test
    void createMenu_nullProductIds_normalizedToEmptyList() {
        MenuRequestDto request = baseRequest();
        request.setProductIds(null);

        when(menuRepository.existsByCode("PHISH")).thenReturn(false);
        when(uniqueIdGenerator.generateUUID()).thenReturn(MENU_ID);
        when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> inv.getArgument(0));
        when(menuPermissionService.generatePermissions(any(Menu.class))).thenReturn(List.of());
        when(permissionRepository.saveAll(anyList())).thenReturn(List.of());

        MenuResponseDto response = menuService.createMenu(request);

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).save(captor.capture());
        assertNotNull(captor.getValue().getProductIds());
        assertTrue(captor.getValue().getProductIds().isEmpty());
        assertTrue(response.getProductIds() == null || response.getProductIds().isEmpty());
    }

    @Test
    void updateMenu_updatesProductIds() {
        Menu existing = new Menu();
        existing.setId(MENU_ID);
        existing.setCode("PHISH");
        existing.setProductIds(List.of(PRODUCT_A));
        existing.setActions(List.of("VIEW"));
        existing.setStatus(MenuStatus.ACTIVE.getMenusStatus());

        MenuRequestDto request = baseRequest();
        request.setProductIds(List.of(PRODUCT_B));

        when(menuRepository.findById(MENU_ID)).thenReturn(Optional.of(existing));
        when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> inv.getArgument(0));
        when(menuPermissionService.generatePermissions(any(Menu.class))).thenReturn(List.of());
        when(permissionRepository.saveAll(anyList())).thenReturn(List.of());

        MenuResponseDto response = menuService.updateMenu(MENU_ID, request);

        assertEquals(List.of(PRODUCT_B), existing.getProductIds());
        assertEquals(List.of(PRODUCT_B), response.getProductIds());
    }

    @Test
    void getMenuById_returnsProductIds() {
        Menu existing = new Menu();
        existing.setId(MENU_ID);
        existing.setCode("PHISH");
        existing.setName("Phishing");
        existing.setProductIds(List.of(PRODUCT_A));

        when(menuRepository.findById(MENU_ID)).thenReturn(Optional.of(existing));

        MenuResponseDto response = menuService.getMenuById(MENU_ID);

        assertEquals(List.of(PRODUCT_A), response.getProductIds());
    }

    @Test
    void getMenusWithSavedPermissions_includesProductIds() {
        Menu menu = new Menu();
        menu.setId(MENU_ID);
        menu.setCode("PHISH");
        menu.setName("Phishing");
        menu.setActions(List.of("VIEW"));
        menu.setProductIds(List.of(PRODUCT_A, PRODUCT_B));

        when(menuRepository.findAll()).thenReturn(List.of(menu));
        when(permissionRepository.findByMenuCodeIn(List.of("PHISH"))).thenReturn(List.of());

        List<MenuWithPermissionsDto> result = menuService.getMenusWithSavedPermissions();

        assertEquals(1, result.size());
        assertEquals(List.of(PRODUCT_A, PRODUCT_B), result.get(0).getProductIds());
    }

    private MenuRequestDto baseRequest() {
        MenuRequestDto request = new MenuRequestDto();
        request.setName("Phishing");
        request.setCode("PHISH");
        request.setSequenceNumber(1);
        request.setUrl("/phishing");
        request.setMenuType(MenuType.MAIN_MENU.getMenuType());
        request.setIcon("shield");
        request.setStatus(MenuStatus.ACTIVE.getMenusStatus());
        request.setActions(List.of("VIEW"));
        return request;
    }
}
