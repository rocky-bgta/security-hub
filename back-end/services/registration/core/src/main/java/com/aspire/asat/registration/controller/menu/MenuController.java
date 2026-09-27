package com.aspire.asat.registration.controller.menu;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.menu.MenuRequestDto;
import com.aspire.asat.registration.data.menu.MenuResponseDto;
import com.aspire.asat.registration.data.menu.MenuWithPermissionsDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@RequestMapping(value = WebApiUrlConstants.API_URI_ROOT+"/menus", produces = "application/json")
public interface MenuController {

    @PostMapping
    ResponseEntity<ApiResponse<MenuResponseDto>> createMenu(@RequestBody @Valid MenuRequestDto request, HttpServletRequest httpRequest);

    @GetMapping
    ResponseEntity<ApiResponse<List<MenuResponseDto>>> getAllMenus(HttpServletRequest httpRequest);

    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<MenuResponseDto>> updateMenu(@PathVariable String id, @RequestBody @Valid MenuRequestDto request, HttpServletRequest httpRequest);

    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable String id, HttpServletRequest httpRequest);

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<MenuResponseDto>> getMenu(@PathVariable String id, HttpServletRequest httpRequest);

    @GetMapping("/with-permissions")
    ResponseEntity<ApiResponse<List<MenuWithPermissionsDto>>> getMenusWithSavedPermissions(HttpServletRequest httpRequest);

}
