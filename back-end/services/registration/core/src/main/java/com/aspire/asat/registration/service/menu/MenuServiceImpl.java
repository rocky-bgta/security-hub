package com.aspire.asat.registration.service.menu;


import com.aspire.asat.registration.data.enums.MenuStatus;
import com.aspire.asat.registration.data.enums.MenuType;
import com.aspire.asat.registration.data.menu.MenuRequestDto;
import com.aspire.asat.registration.data.menu.MenuResponseDto;
import com.aspire.asat.registration.data.menu.MenuWithPermissionsDto;
import com.aspire.asat.registration.data.menu.PermissionDto;
import com.aspire.asat.registration.exception.CustomException;
import com.aspire.asat.registration.model.menu.Menu;
import com.aspire.asat.registration.model.menu.Permission;
import com.aspire.asat.registration.repository.menus.MenuRepository;
import com.aspire.asat.registration.repository.menus.PermissionRepository;
import com.aspire.asat.registration.utils.UniqueIdGenerator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final ModelMapper modelMapper;
    private final MenuPermissionService menuPermissionService;
    private final UniqueIdGenerator uniqueIdGenerator;

    @Override
    public MenuResponseDto createMenu(MenuRequestDto request) {
        if (menuRepository.existsByCode(request.getCode())) {
            throw new CustomException("menu.code.exists");
        }

        // Validate based on a menu type
        if (MenuType.MAIN_MENU.getMenuType().equalsIgnoreCase(request.getMenuType())) {
            if (request.getParentMenuId() != null) {
                throw new CustomException("Main menu should not have a parent menu.");
            }
        } else if (MenuType.SUB_MENU.getMenuType().equalsIgnoreCase(request.getMenuType())) {
            if (request.getParentMenuId() == null) {
                throw new CustomException("Sub menu must have a valid parent menu.");
            }
            validateParentMenu(request.getParentMenuId());
        } else {
            throw new CustomException("Invalid menu type provided.");
        }

        Menu menu = modelMapper.map(request, Menu.class);
        menu.setId(uniqueIdGenerator.generateUUID());
        if (menu.getProductIds() == null) {
            menu.setProductIds(List.of());
        }

        Menu saved = menuRepository.save(menu);
        permissionRepository.saveAll(menuPermissionService.generatePermissions(saved));

        return modelMapper.map(saved, MenuResponseDto.class);
    }


    @Override
    public List<MenuResponseDto> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(menu -> modelMapper.map(menu, MenuResponseDto.class))
                .toList();
    }

    @Override
    public MenuResponseDto updateMenu(String id, MenuRequestDto request) {
        Menu existing = menuRepository.findById(id)
                .orElseThrow(() -> new CustomException("menu.not.found"));

        // Check if code is being changed and the new one already exists
        if (!existing.getCode().equals(request.getCode()) &&
                menuRepository.existsByCode(request.getCode())) {
            throw new CustomException("menu.code.exists");
        }

        validateParentMenu(request.getParentMenuId());

        // Prevent activating a menu if its parent is inactive
        if (MenuStatus.ACTIVE.getMenusStatus().equalsIgnoreCase(request.getStatus()) && request.getParentMenuId() != null) {
            Menu parentMenu = menuRepository.findById(request.getParentMenuId())
                    .orElseThrow(() -> new CustomException("parent.menu.not.found"));
            if (MenuStatus.INACTIVE.getMenusStatus().equalsIgnoreCase(parentMenu.getStatus())) {
                throw new CustomException("Cannot activate menu when parent menu is inactive.");
            }
        }

        existing.setActions(request.getActions());
        existing.setMenuType(request.getMenuType());
        existing.setName(request.getName());
        existing.setParentMenuId(request.getParentMenuId());
        existing.setStatus(request.getStatus());
        existing.setCode(request.getCode());
        //existing.setId(id);
        existing.setUrl(request.getUrl());
        existing.setCode(request.getCode());
        existing.setIcon(request.getIcon());
        existing.setSequenceNumber(request.getSequenceNumber());
        existing.setProductIds(request.getProductIds() != null ? request.getProductIds() : List.of());
        Menu updated = menuRepository.save(existing);

        // Refresh permissions
        permissionRepository.deleteByMenuCode(updated.getCode());
        permissionRepository.saveAll(menuPermissionService.generatePermissions(updated));

        // If status is set to inactive, recursively set all child menus to inactive
        if (MenuStatus.INACTIVE.getMenusStatus().equalsIgnoreCase(updated.getStatus())) {
            setChildMenusInactive(updated.getId());
        }

        return modelMapper.map(updated, MenuResponseDto.class);
    }

    /**
     * Recursively set all child menus to inactive
     */
    private void setChildMenusInactive(String parentMenuId) {
        List<Menu> childMenus = menuRepository.findByParentMenuId(parentMenuId);
        for (Menu child : childMenus) {
                child.setStatus(MenuStatus.INACTIVE.getMenusStatus());
                menuRepository.save(child);
                setChildMenusInactive(child.getId()); // Recursive call
        }
    }

    @Override
    public void deleteMenu(String id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new CustomException("menu.not.found"));

        menuRepository.delete(menu);
        permissionRepository.deleteByMenuCode(menu.getCode());
    }

    @Override
    public MenuResponseDto getMenuById(String id) {
        Menu existing = menuRepository.findById(id)
                .orElseThrow(() -> new CustomException("menu.not.found"));
        return modelMapper.map(existing, MenuResponseDto.class);
    }


    @Override
    public List<MenuWithPermissionsDto> getMenusWithSavedPermissions() {
        List<Menu> menus = menuRepository.findAll();
        List<String> menuCodes = menus.stream().map(Menu::getCode).toList();

        List<Permission> allPermissions = permissionRepository.findByMenuCodeIn(menuCodes);
        Map<String, List<Permission>> permissionMap = allPermissions.stream()
                .collect(Collectors.groupingBy(Permission::getMenuCode));

        return menus.stream().map(menu -> {
            MenuWithPermissionsDto dto = new MenuWithPermissionsDto();
            dto.setId(menu.getId());
            dto.setName(menu.getName());
            dto.setCode(menu.getCode());
            dto.setActions(menu.getActions());
            dto.setProductIds(menu.getProductIds());

            List<PermissionDto> permissionDto = permissionMap
                    .getOrDefault(menu.getCode(), List.of())
                    .stream()
                    .map(permission -> {
                        PermissionDto pDto = new PermissionDto();
                        pDto.setId(permission.getId());
                        pDto.setName(permission.getName());
                        pDto.setMenuCode(permission.getMenuCode());
                        pDto.setAction(permission.getAction());
                        return pDto;
                    })
                    .toList();

            dto.setPermissions(permissionDto);
            return dto;
        }).toList();
    }

    /**
     * Helper method to validate parent menu existence
     */
    private void validateParentMenu(String parentMenuId) {
        if (parentMenuId != null && !menuRepository.existsById(parentMenuId)) {
            throw new CustomException("menu.code.not.found");
        }
    }
}
