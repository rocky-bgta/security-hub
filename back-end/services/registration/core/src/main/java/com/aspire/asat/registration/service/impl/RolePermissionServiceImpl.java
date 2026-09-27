package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.menu.UserMenuResponse;
import com.aspire.asat.registration.data.roles.ActionPermissionDTO;
import com.aspire.asat.registration.data.roles.MenuPermissionResponseDTO;
import com.aspire.asat.registration.data.roles.RolePermissionDto;
import com.aspire.asat.registration.data.roles.UserMenuPermissionResponse;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.model.RolePermission;
import com.aspire.asat.registration.model.menu.Menu;
import com.aspire.asat.registration.model.menu.Permission;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.RolePermissionRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.repository.menus.MenuRepository;
import com.aspire.asat.registration.repository.menus.PermissionRepository;
import com.aspire.asat.registration.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolePermissionServiceImpl implements RolePermissionService {

    private static final String ACTIVE_LICENSE_STATUS = "ACTIVE";

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final AspireUserRepository aspireUserRepository;
    private final ClientProductRepository clientProductRepository;

    @Override
    public RolePermissionDto saveRolePermission(RolePermissionDto rolePermissionDto) {

        // Generate permissions from menuCode:permittedActions if not provided
        if (rolePermissionDto.getMenuPermissions() != null) {
            rolePermissionDto.getMenuPermissions().forEach(menuPermission -> {
                if (menuPermission.getPermissions() == null || menuPermission.getPermissions().isEmpty()) {
                    // Generate permissions from menuCode:permittedActions
                    List<String> generatedPermissions = generatePermissionsFromActions(
                            menuPermission.getMenuCode(),
                            menuPermission.getPermittedActions()
                    );
                    menuPermission.setPermissions(generatedPermissions);
                }
            });
        }

        Optional<RolePermission> existingRolePermission = rolePermissionRepository.findByRoleId(rolePermissionDto.getRoleId());
        if (existingRolePermission.isPresent()) {
            RolePermission rolePermission = existingRolePermission.get();
            if (rolePermissionDto.getMenuPermissions() != null) {
                rolePermission.setMenuPermissions(rolePermissionDto.getMenuPermissions());
            }
            if (rolePermissionDto.getRoleName() != null) {
                rolePermission.setRoleName(rolePermissionDto.getRoleName());
            }
            return RolePermission.toRolePermissionDto(rolePermissionRepository.save(rolePermission));
        } else {
            RolePermission newRolePermission = RolePermission.toRolePermission(rolePermissionDto);
            return RolePermission.toRolePermissionDto(rolePermissionRepository.save(newRolePermission));
        }
    }


    @Override
    public List<MenuPermissionResponseDTO> getMenuPermissionsByRoleId(String roleId, String search) {

        if (!StringUtils.hasText(roleId)) {
            throw new IllegalArgumentException("Role ID cannot be null or empty");
        }

        // Validate that the role exists
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role with ID '" + roleId + "' not found"));

        // 1. Fetch all possible menu structures.
        List<Menu> allMenus = menuRepository.findAll();

        // Filter menus by name if search parameter is provided
        List<Menu> filteredMenus = allMenus;
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.trim().toLowerCase();
            filteredMenus = allMenus.stream()
                    .filter(menu -> menu.getName() != null && 
                            menu.getName().toLowerCase().contains(searchTerm))
                    .toList();
        }

        // 2. Get the set of permitted actions for the role (e.g., "USR-MGT:VIEW").
        Set<String> permittedActions = getPermittedActionsForRole(roleId);

        // 3. For each menu, build the final response DTO by merging the menu
        // structure with the role's specific permissions.
        return filteredMenus.stream()
                .map(menu -> buildResponseDTOForMenu(menu, permittedActions))
                .toList();
    }

    /**
     * Fetches a role's permissions from the database and returns them as a Set for fast lookups.
     */
    private Set<String> getPermittedActionsForRole(String roleId) {
        return rolePermissionRepository.findByRoleId(roleId)
                .map(rolePermission -> {
                    if (rolePermission.getMenuPermissions() == null) {
                        return Collections.<String>emptySet();
                    }
                    return rolePermission.getMenuPermissions().stream()
                            // Create the full permission string like "MENU_CODE:ACTION"
                            .flatMap(menuPerm -> {
                                if (menuPerm.getPermittedActions() == null) {
                                    return Stream.empty();
                                }
                                return menuPerm.getPermittedActions().stream()
                                        .map(action -> menuPerm.getMenuCode() + ":" + action);
                            })
                            .collect(Collectors.toSet());
                })
                .orElse(Collections.emptySet()); // Return an empty set if the role has no permissions
    }

    /**
     * Constructs a MenuPermissionResponseDTO for a single menu.
     */
    private MenuPermissionResponseDTO buildResponseDTOForMenu(Menu menu, Set<String> permittedActions) {
        // Determine which actions are selected for this menu
        List<ActionPermissionDTO> actionPermissions = menu.getActions().stream()
                .map(action -> {
                    String fullPermissionName = menu.getCode() + ":" + action;
                    boolean isSelected = permittedActions.contains(fullPermissionName);
                    return new ActionPermissionDTO(action.toLowerCase(), isSelected);
                })
                .toList();

        // The parent menu is "selected" if any of its actions are selected
        boolean isMenuSelected = actionPermissions.stream().anyMatch(ActionPermissionDTO::getSelected);

        // Build and return the final DTO
        return new MenuPermissionResponseDTO(
                menu.getId(),
                menu.getCode(),
                menu.getName(),
                menu.getUrl(),
                menu.getMenuType(),
                menu.getSequenceNumber(),
                menu.getParentMenuId(),
                isMenuSelected,
                actionPermissions
        );
    }

    @Override
    public List<UserMenuPermissionResponse> getCurrentUserRolePermissions(CurrentUserContext userContext, String username) {
        // Check if the user is superadmin
        if (isSuperAdmin(username)) {
            return getAllPermissionsForSuperAdmin(username);
        }

        AspireUser aspireUser = aspireUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        List<String> userRoleIds = aspireUser.getRoles() != null ? aspireUser.getRoles() : List.of();

        if (userRoleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<RolePermission> rolePermissions = rolePermissionRepository.findRolePermissionsByRoleIds(userRoleIds);

        String clientAdminId = resolveClientAdminId(userContext, aspireUser);
        Set<String> purchasedProductIds = loadPurchasedProductIds(clientAdminId);
        boolean applyProductFilter = StringUtils.hasText(clientAdminId);

        return rolePermissions.stream()
                .map(rolePermission -> {
                    List<UserMenuResponse> userMenus = rolePermission.getMenuPermissions().stream()
                            .filter(menuPermission -> menuPermission.getPermissions() != null
                                    && !menuPermission.getPermissions().isEmpty())
                            .map(menuPermission -> menuRepository.findById(menuPermission.getMenuId())
                                    .filter(menu -> !applyProductFilter
                                            || isMenuAllowedForProducts(menu, purchasedProductIds))
                                    .map(menu -> UserMenuResponse.builder()
                                            .id(menu.getId())
                                            .code(menu.getCode())
                                            .name(menu.getName())
                                            .url(menu.getUrl())
                                            .icon(menu.getIcon())
                                            .menuType(menu.getMenuType())
                                            .sequenceNumber(menu.getSequenceNumber())
                                            .parentMenuId(menu.getParentMenuId())
                                            .permittedActions(menuPermission.getPermittedActions())
                                            .permissions(menuPermission.getPermissions())
                                            .build())
                                    .orElse(null))
                            .filter(Objects::nonNull)
                            .toList();

                    return new UserMenuPermissionResponse(
                            rolePermission.getRoleId(),
                            rolePermission.getRoleName(),
                            userMenus
                    );
                })
                .toList();
    }

    /**
     * Prefer CurrentUserContext.clientAdminId; fall back to AspireUser.clientAdminId.
     */
    private String resolveClientAdminId(CurrentUserContext userContext, AspireUser aspireUser) {
        if (userContext != null && StringUtils.hasText(userContext.getClientAdminId())) {
            return userContext.getClientAdminId();
        }
        if (aspireUser != null && StringUtils.hasText(aspireUser.getClientAdminId())) {
            return aspireUser.getClientAdminId();
        }
        return null;
    }

    private Set<String> loadPurchasedProductIds(String clientAdminId) {
        if (!StringUtils.hasText(clientAdminId)) {
            return Collections.emptySet();
        }
        return clientProductRepository
                .findByClientAdminIdAndLicenseStatus(clientAdminId, ACTIVE_LICENSE_STATUS)
                .stream()
                .map(ClientProduct::getProductId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    /**
     * Shared menus (null/empty productIds) are always allowed.
     * Product-bound menus require at least one intersecting ACTIVE purchase.
     */
    boolean isMenuAllowedForProducts(Menu menu, Set<String> purchasedProductIds) {
        List<String> menuProductIds = menu.getProductIds();
        if (menuProductIds == null || menuProductIds.isEmpty()) {
            return true;
        }
        if (purchasedProductIds == null || purchasedProductIds.isEmpty()) {
            return false;
        }
        return menuProductIds.stream().anyMatch(purchasedProductIds::contains);
    }

    private List<String> generatePermissionsFromActions(String menuCode, List<String> permittedActions) {
        if (menuCode == null || permittedActions == null || permittedActions.isEmpty()) {
            return Collections.emptyList();
        }

        return permittedActions.stream()
                .map(action -> menuCode + ":" + action)
                .toList();
    }

    /**
     * Check if the user is a superadmin
     */
    private boolean isSuperAdmin(String username) {
        AspireUser aspireUser = aspireUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return UserType.SUPER_ADMIN.name().equalsIgnoreCase(aspireUser.getUserType());
    }

    /**
     * Get all permissions for superadmin user
     */
    private List<UserMenuPermissionResponse> getAllPermissionsForSuperAdmin(String username) {
        // Get all menus
        List<Menu> allMenus = menuRepository.findAll();

        // Get all permissions for all menus
        List<String> menuCodes = allMenus.stream().map(Menu::getCode).toList();
        List<Permission> allPermissions = permissionRepository.findByMenuCodeIn(menuCodes);

        // Group permissions by menu code
        Map<String, List<Permission>> permissionMap = allPermissions.stream()
                .collect(Collectors.groupingBy(Permission::getMenuCode));

        // Build user menu responses with all permissions
        List<UserMenuResponse> userMenus = allMenus.stream()
                .map(menu -> {
                    List<Permission> menuPermissions = permissionMap.getOrDefault(menu.getCode(), Collections.emptyList());

                    // Get all actions for this menu
                    List<String> allActions = menu.getActions() != null ? menu.getActions() : Collections.emptyList();

                    // Generate all possible permissions for this menu
                    List<String> allMenuPermissions = menuPermissions.stream()
                            .map(Permission::getName)
                            .toList();

                    return UserMenuResponse.builder()
                            .id(menu.getId())
                            .code(menu.getCode())
                            .name(menu.getName())
                            .url(menu.getUrl())
                            .icon(menu.getIcon())
                            .menuType(menu.getMenuType())
                            .sequenceNumber(menu.getSequenceNumber())
                            .parentMenuId(menu.getParentMenuId())
                            .permittedActions(allActions) // All actions are permitted
                            .permissions(allMenuPermissions) // All permissions are granted
                            .build();
                })
                .toList();

        AspireUser aspireUser = aspireUserRepository.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("User not found with username: " + username));
        return List.of(new UserMenuPermissionResponse(
                resolveSuperAdminRoleId(aspireUser),
                UserType.SUPER_ADMIN.name(),
                userMenus
        ));
    }

    private String resolveSuperAdminRoleId(AspireUser aspireUser) {
        if (aspireUser.getRoles() != null && !aspireUser.getRoles().isEmpty()) {
            return aspireUser.getRoles().get(0);
        }

        var superAdminRole = roleRepository.findByRoleName(UserType.SUPER_ADMIN.name());
        return superAdminRole != null ? superAdminRole.getId() : UserType.SUPER_ADMIN.name();
    }

}
