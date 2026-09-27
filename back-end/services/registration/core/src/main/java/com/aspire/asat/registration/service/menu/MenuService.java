package com.aspire.asat.registration.service.menu;

import com.aspire.asat.registration.data.menu.MenuRequestDto;
import com.aspire.asat.registration.data.menu.MenuResponseDto;
import com.aspire.asat.registration.data.menu.MenuWithPermissionsDto;

import java.util.List;

public interface MenuService {
    MenuResponseDto createMenu(MenuRequestDto request);
    List<MenuResponseDto> getAllMenus();
    MenuResponseDto updateMenu(String id, MenuRequestDto request);
    void deleteMenu(String id);
    MenuResponseDto getMenuById(String id);
    List<MenuWithPermissionsDto> getMenusWithSavedPermissions();

}