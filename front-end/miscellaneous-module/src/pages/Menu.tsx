import { Edit, Menu as MenuIcon, Plus, Search, Trash2 } from 'lucide-react';
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
import { Input } from 'common/Input';
import { GetMenuIcon } from 'components/SidebarMenuIcons';
import ActionMenu from 'features/menu/ActionMenu';
import DeleteMenu from 'features/menu/DeleteMenu';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { IMenu, MenuType } from 'models/Menu';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn } from 'utils/Helper';

const getActionBadgeColor = (action: string) => {
  const colors = {
    view: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
    create: 'bg-green-500/20 text-green-400 border-green-500/30',
    edit: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    delete: 'bg-red-500/20 text-red-400 border-red-500/30',
    export: 'bg-purple-500/20 text-purple-400 border-purple-500/30',
    publish: 'bg-indigo-500/20 text-indigo-400 border-indigo-500/30',
    assign: 'bg-orange-500/20 text-orange-400 border-orange-500/30',
  };
  return (
    colors[action.toLowerCase() as keyof typeof colors] ||
    'bg-gray-500/20 text-gray-400 border-gray-500/30'
  );
};

const Menu = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [menus, setMenus] = useState<Array<IMenu>>([]);
  const [isDeleteMenuOpen, setIsDeleteMenuOpen] = useState<boolean>(false);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [selectedMenu, setSelectedMenu] = useState<IMenu | null>(null);

  const apiClient = useAPI();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const response: IResponse<Array<IMenu>> = await apiClient.get(
        API_END_POINTS.GET_MENU_LIST,
      );

      const parentMenus = response.data
        .filter(menu => menu.menuType === MenuType.MAIN_MENU)
        .sort((a, b) => a.sequenceNumber - b.sequenceNumber);

      const childMenus = response.data.filter(
        menu => menu.menuType === MenuType.SUB_MENU,
      );

      parentMenus.forEach(parent => {
        parent.children = childMenus
          .filter(child => child.parentMenuId === parent.id)
          .sort((a, b) => a.sequenceNumber - b.sequenceNumber);
      });

      setMenus(parentMenus);
    } catch (error) {
      console.error('Error fetching menu data:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredMenus = menus?.filter(
    menu =>
      menu.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      menu.url.toLowerCase().includes(searchTerm.toLowerCase()) ||
      menu.children?.some(
        submenu =>
          submenu.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          submenu.url.toLowerCase().includes(searchTerm.toLowerCase()),
      ),
  );

  const parentMenus = menus.filter(
    menu => menu.menuType === MenuType.MAIN_MENU,
  );

  const handleDeleteMenu = () => {
    toast.success(`Menu ${selectedMenu?.name} has been successfully deleted.`);
    fetchData();
    setSelectedMenu(null);
    setIsDeleteMenuOpen(false);
  };

  const handleSubmitMenu = () => {
    fetchData();
    setSelectedMenu(null);
    setActionType(null);
  };

  const renderPermissions = (menu: IMenu) => {
    return (
      <div className="flex justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-3">
            <div className="flex size-8 items-center justify-center rounded-lg">
              {GetMenuIcon(menu.icon)}
            </div>

            <div>
              <h3 className="font-medium text-foreground">{menu.name}</h3>
              <p className="text-sm text-muted-foreground">{menu.url}</p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="flex flex-wrap gap-1">
            {menu.actions?.map((action: string) => (
              <Badge
                key={action}
                className={cn('text-xs', getActionBadgeColor(action))}
              >
                {action}
              </Badge>
            ))}
          </div>

          <Badge
            variant={menu.status === 'ACTIVE' ? 'default' : 'secondary'}
            className="capitalize"
          >
            {menu.status.toLowerCase()}
          </Badge>

          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setActionType('edit');
                setSelectedMenu(menu);
              }}
            >
              <Edit className="size-4" />
            </Button>
            {/* <Button
              variant="ghost"
              size="sm"
              className="hover:bg-destructive/20 hover:text-destructive"
              onClick={() => {
                setSelectedMenu(menu);
                setIsDeleteMenuOpen(true);
              }}
            >
              <Trash2 className="h-4 w-4" />
            </Button> */}
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Menu Management
          </h1>
          <p className="text-muted-foreground">
            Manage system menus and navigation structure
          </p>
        </div>
        <Button
          className="text-secondary"
          onClick={() => setActionType('create')}
        >
          <Plus className="mr-2 size-4" />
          Add Menu
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <MenuIcon className="size-5 text-primary" />
            Menu Structure
          </CardTitle>
          <CardDescription>
            View and manage all system menus and submenus
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-6 flex items-center gap-4">
            <div className="relative max-w-sm flex-1">
              <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search menus..."
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>

          <div className="space-y-4">
            {loading || filteredMenus?.length === 0 ? (
              <p className="text-center text-sm text-muted-foreground">
                {loading ? 'Loading...' : 'No menus available.'}
              </p>
            ) : (
              <Accordion type="single" collapsible className="w-full">
                {filteredMenus?.map(menu => (
                  <AccordionItem
                    key={menu.id}
                    value={menu.id + ''}
                    className="border-b-0 pr-4"
                  >
                    <Card>
                      <CardContent className="mb-4 py-0">
                        {menu.children?.length === 0 ? (
                          <div className="w-full space-y-4 py-4 text-left">
                            {renderPermissions(menu)}
                          </div>
                        ) : (
                          <AccordionHeader>
                            <div className="w-full space-y-4 py-4 text-left">
                              {renderPermissions(menu)}
                            </div>
                            <AccordionTrigger></AccordionTrigger>
                          </AccordionHeader>
                        )}

                        {menu.children?.length !== 0 && (
                          <AccordionContent>
                            {menu.children?.map(submenu => (
                              <div
                                key={submenu.id}
                                className="mb-4 rounded-lg border border-graphite p-4"
                              >
                                {renderPermissions(submenu)}
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

      <ActionMenu
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedMenu(null);
        }}
        menu={selectedMenu}
        parentMenuList={parentMenus}
        onSubmit={handleSubmitMenu}
      />

      {selectedMenu && (
        <DeleteMenu
          isOpen={isDeleteMenuOpen}
          onClose={() => {
            setIsDeleteMenuOpen(false);
            setSelectedMenu(null);
          }}
          menu={selectedMenu}
          onDeleteMenu={handleDeleteMenu}
        />
      )}
    </div>
  );
};

export default Menu;
