import { RotateCcw, Save, Search } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import {
  Accordion,
  AccordionContent,
  AccordionHeader,
  AccordionItem,
  AccordionTrigger,
} from 'common/Accordion';
import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Checkbox } from 'common/Checkbox';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { IMenuWithPermissions, MenuAction, MenuType } from 'models/Menu';
import { IRole } from 'models/Role';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { Input } from 'common/Input';

const permissionLabels = {
  [MenuAction.VIEW]: 'View',
  [MenuAction.CREATE]: 'Create',
  [MenuAction.EDIT]: 'Edit',
  [MenuAction.DELETE]: 'Delete',
  [MenuAction.EXPORT]: 'Export',
  [MenuAction.PUBLISH]: 'Publish',
  [MenuAction.ASSIGN]: 'Assign',
};

const Permissions = () => {
  const [loading, setLoading] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [roles, setRoles] = useState<Array<IRole>>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');

  const [menuData, setMenuData] = useState<Array<IMenuWithPermissions>>([]);
  const [structuredMenuData, setStructuredMenuData] = useState<
    Array<IMenuWithPermissions>
  >([]);
  const [selectedRole, setSelectedRole] = useState<IRole | null>(null);
  const [permissions, setPermissions] = useState<Record<string, boolean>>({});

  const apiClient = useAPI();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const response: IResponse<Array<IRole>> = await apiClient.get(
        API_END_POINTS.GET_ROLE_LIST + 'sortBy=accessLevel&order=desc',
      );
      setRoles(response.data);
      if (response.data.length > 0) {
        setSelectedRole(response.data[0]);
        initializePermissions(response.data[0].id);
      }
    } catch (error) {
      console.error('Error fetching role data:', error);
    }
  };

  const initializePermissions = async (
    roleId: string,
    reset: boolean = false,
  ) => {
    let data = menuData;
    if (!reset) {
      try {
        setLoading(true);
        const response: IResponse<Array<IMenuWithPermissions>> =
          await apiClient.get(
            API_END_POINTS.GET_ROLE_MENU_PERMISSIONS.replace(':id', roleId),
          );

        setMenuData(response.data);

        data = response.data;
      } catch (error) {
        console.error('Error fetching role menu permissions:', error);
      } finally {
        setLoading(false);
      }
    }

    const parentMenus = data
      .filter(menu => menu.menuType === MenuType.MAIN_MENU)
      .sort((a, b) => a.sequenceNumber - b.sequenceNumber);

    const childMenus = data.filter(menu => menu.menuType === MenuType.SUB_MENU);

    parentMenus.forEach(parent => {
      parent.children = childMenus
        .filter(child => child.parentMenuId === parent.menuId)
        .sort((a, b) => a.sequenceNumber - b.sequenceNumber);
    });

    setStructuredMenuData(parentMenus);

    const newPermissions: Record<string, boolean> = {};

    parentMenus.forEach(menu => {
      menu.actions.forEach(item => {
        const key = `${menu.menuCode}:${item.action}`;
        newPermissions[key] = item.selected;
      });

      menu.children?.forEach(submenu => {
        submenu.actions.forEach(item => {
          const key = `${submenu.menuCode}:${item.action}`;
          newPermissions[key] = item.selected;
        });
      });
    });

    setPermissions(newPermissions);
  };

  const handleRoleChange = (roleId: string) => {
    setSelectedRole(roles.find(r => r.id === roleId) ?? null);
    initializePermissions(roleId);
  };

  const handlePermissionChange = (key: string, selected: boolean) => {
    setPermissions(prev => ({
      ...prev,
      [key]: selected,
    }));
  };

  const handleSave = async () => {
    const menuMap: Record<string, Array<string>> = {};
    for (const key in permissions) {
      if (permissions[key] === false) continue;

      const [menuCode, action] = key.split(':');
      if (!(menuCode in menuMap)) {
        menuMap[menuCode] = [];
      }

      menuMap[menuCode].push(action.toUpperCase());
    }
    setIsSubmitting(true);

    const payload = {
      roleId: selectedRole?.id,
      roleName: selectedRole?.roleName,
      menuPermissions: menuData.map(menu => ({
        menuId: menu.menuId,
        menuCode: menu.menuCode,
        permittedActions: menuMap[menu.menuCode],
      })),
    };

    try {
      await apiClient.post(API_END_POINTS.UPDATE_ROLE_PERMISSIONS, {
        data: payload,
      });

      toast.success(
        `Permissions for ${
          roles.find(r => r.id === selectedRole?.id)?.roleName
        } have been updated successfully.`,
      );
    } catch (error) {
      console.error('Error updating role permissions:', error);
      toast.error('Failed to update permissions. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReset = () => {
    initializePermissions(selectedRole?.id!, true);
    toast.success('Permissions have been reset to default values.');
  };

  const filteredMenus = structuredMenuData?.filter(
    menu =>
      menu.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      menu.url.toLowerCase().includes(searchTerm.toLowerCase()) ||
      menu.children?.some(
        submenu =>
          submenu.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          submenu.url.toLowerCase().includes(searchTerm.toLowerCase()),
      ),
  );

  const renderPermissions = (
    id: string,
    menuActions: Array<{ action: string; selected: boolean }>,
  ) => {
    const menuPermissions = menuActions.map(action => action.action);
    return (
      <div className="grid grid-cols-2 gap-4 md:grid-cols-4 lg:grid-cols-7">
        {menuPermissions.map(permission => {
          const key = `${id}:${permission}`;
          const isChecked = permissions[key];

          return (
            <div key={permission} className="flex items-center space-x-2">
              <Checkbox
                id={key}
                checked={isChecked}
                onClick={e => e.stopPropagation()}
                onCheckedChange={checked =>
                  handlePermissionChange(key, checked as boolean)
                }
              />
              <Label
                htmlFor={key}
                className="cursor-pointer text-sm font-medium text-foreground"
              >
                {
                  permissionLabels[
                    permission.toUpperCase() as keyof typeof permissionLabels
                  ]
                }
              </Label>
            </div>
          );
        })}
      </div>
    );
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">
          Permission Matrix
        </h1>
        <p className="text-muted-foreground">
          Manage role permissions for menus and functionalities
        </p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <CardTitle>Role Permissions</CardTitle>
              <CardDescription>
                Configure permissions for each role and menu item
              </CardDescription>
            </div>
            <div className="flex items-center gap-4">
              <Button variant="outline" onClick={handleReset}>
                <RotateCcw className="mr-2 size-4" />
                Reset
              </Button>
              <Button onClick={handleSave} disabled={isSubmitting}>
                <Save className="mr-2 size-4" />
                {isSubmitting ? 'Saving...' : 'Save Changes'}
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="flex items-center justify-between gap-4">
            <div className="flex items-center gap-4">
              <Label className="text-sm font-medium text-foreground">
                Select Role:
              </Label>
              <Select
                value={selectedRole?.id ?? ''}
                onValueChange={handleRoleChange}
              >
                <SelectTrigger className="w-64">
                  <SelectValue placeholder="Select a role" />
                </SelectTrigger>
                <SelectContent>
                  {roles.map(role => (
                    <SelectItem key={role.id} value={role.id}>
                      <div className="flex items-center gap-2">
                        {role.roleName}
                        <Badge variant="secondary" className="text-xs">
                          Level {role.accessLevel}
                        </Badge>
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="relative w-full flex-1">
              <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search..."
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>

          <div className="space-y-4">
            {loading || filteredMenus.length === 0 ? (
              <p className="text-center text-sm text-muted-foreground">
                {loading ? 'Loading...' : 'No menus available.'}
              </p>
            ) : (
              <Accordion type="single" collapsible className="w-full">
                {filteredMenus.map(menu => (
                  <AccordionItem
                    key={menu.menuId}
                    value={menu.menuId}
                    className="border-b-0"
                  >
                    <Card>
                      <CardContent className="mb-4 py-0">
                        {menu.children!.length === 0 ? (
                          <div className="w-full space-y-4 py-4 text-left">
                            <h3 className="text-foreground">{menu.name}</h3>
                          </div>
                        ) : (
                          <AccordionHeader>
                            <AccordionTrigger className="items-baseline">
                              <h3 className="text-foreground">{menu.name}</h3>
                            </AccordionTrigger>
                          </AccordionHeader>
                        )}

                        {menu.actions.length > 0 && (
                          <div className="flex w-full pb-4">
                            {renderPermissions(menu.menuCode, menu.actions)}
                          </div>
                        )}

                        {menu.children!.length > 0 && (
                          <AccordionContent className="space-y-4">
                            {menu.children!.map(submenu => (
                              <div
                                key={submenu.menuId}
                                className="rounded-lg border border-graphite p-4"
                              >
                                <h4 className="mb-3 text-foreground">
                                  {submenu.name}
                                </h4>

                                {renderPermissions(
                                  submenu.menuCode,
                                  submenu.actions,
                                )}
                              </div>
                            ))}
                          </AccordionContent>
                        )}
                      </CardContent>
                    </Card>
                  </AccordionItem>
                ))}
              </Accordion>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Permissions;
