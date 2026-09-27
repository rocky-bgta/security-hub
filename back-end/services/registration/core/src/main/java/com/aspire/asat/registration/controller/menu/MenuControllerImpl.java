package com.aspire.asat.registration.controller.menu;


import com.aspire.asat.registration.controller.base.BaseController;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.enums.ResponseMessage;
import com.aspire.asat.registration.data.menu.MenuRequestDto;
import com.aspire.asat.registration.data.menu.MenuResponseDto;
import com.aspire.asat.registration.data.menu.MenuWithPermissionsDto;
import com.aspire.asat.registration.service.menu.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuControllerImpl extends BaseController implements MenuController {

    private final MenuService menuService;

    @Override
    public ResponseEntity<ApiResponse<MenuResponseDto>> createMenu(MenuRequestDto request, HttpServletRequest httpRequest) {
        return handleRequest(
                () -> menuService.createMenu(request),
                ResponseMessage.MENU_CREATED.getResponseMessage(),
                httpRequest
        );
    }

    @Override
    public ResponseEntity<ApiResponse<List<MenuResponseDto>>> getAllMenus(HttpServletRequest httpRequest) {
        return handleRequest(
                menuService::getAllMenus,
                ResponseMessage.FETCHED_SUCCESS.getResponseMessage(),
                httpRequest
        );
    }

    @Override
    public ResponseEntity<ApiResponse<MenuResponseDto>> updateMenu(String id,MenuRequestDto request, HttpServletRequest httpRequest) {
        return handleRequest(
                () -> menuService.updateMenu(id, request),
                ResponseMessage.MENU_UPDATED.getResponseMessage(),
                httpRequest
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteMenu(String id, HttpServletRequest httpRequest) {
        return handleRequest(
                () -> menuService.deleteMenu(id),
                ResponseMessage.MENU_DELETED.getResponseMessage(),
                httpRequest
        );
    }

    @Override
    public ResponseEntity<ApiResponse<MenuResponseDto>> getMenu(String id, HttpServletRequest httpRequest) {
        return handleRequest(
                () -> menuService.getMenuById(id),
                ResponseMessage.OPERATION_SUCCESSFUL.getResponseMessage(),
                httpRequest
        );
    }

    @Override
    public ResponseEntity<ApiResponse<List<MenuWithPermissionsDto>>> getMenusWithSavedPermissions(HttpServletRequest httpRequest) {
        return handleRequest(
                menuService::getMenusWithSavedPermissions,
                ResponseMessage.FETCHED_SUCCESS.getResponseMessage(),
                httpRequest
        );
    }

}


