import { Save, X } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import CustomSelect from 'common/CustomSelect';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { GetMenuIcon, SidebarMenuIcons } from 'components/SidebarMenuIcons';
import { useAPI } from 'hooks/UseAPI';
import { IList, IResponse } from 'models/Global';
import {
  ISelectOption,
  TMultiValue,
  TSingleValue,
} from 'models/Input';
import { IMenu, IMenuPayload, MenuAction, MenuType } from 'models/Menu';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProductListItem {
  id?: string;
  name?: string;
  productId?: string;
  productName?: string;
}

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  menu: IMenu | null;
  parentMenuList: Array<IMenu>;
  onSubmit: (data: IMenu) => void;
}

const actionOptions = [
  {
    id: MenuAction.VIEW,
    label: 'View',
    description: 'Allow users to view this menu',
  },
  {
    id: MenuAction.CREATE,
    label: 'Create',
    description: 'Allow users to create new items',
  },
  {
    id: MenuAction.EDIT,
    label: 'Edit',
    description: 'Allow users to edit existing items',
  },
  {
    id: MenuAction.DELETE,
    label: 'Delete',
    description: 'Allow users to delete items',
  },
  {
    id: MenuAction.EXPORT,
    label: 'Export',
    description: 'Allow users to export data',
  },
  {
    id: MenuAction.PUBLISH,
    label: 'Publish',
    description: 'Allow users to publish content',
  },
  {
    id: MenuAction.ASSIGN,
    label: 'Assign',
    description: 'Allow users to assign items to others',
  },
];

const DefaultMenu: IMenuPayload = {
  name: '',
  code: '',
  icon: '',
  url: '',
  parentMenuId: '',
  sequenceNumber: 1,
  actions: [],
  status: 'ACTIVE',
  menuType: MenuType.MAIN_MENU,
  productIds: [],
};

const ActionMenu = ({
  isOpen,
  onClose,
  menu,
  parentMenuList,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IMenuPayload>({ ...DefaultMenu });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [productOptions, setProductOptions] = useState<Array<ISelectOption>>(
    [],
  );
  const [productsLoading, setProductsLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    if (menu) {
      const { id, children, ...rest } = menu;
      setFormData({
        ...rest,
        productIds: rest.productIds ?? [],
      });
    } else {
      setFormData({ ...DefaultMenu });
    }
  }, [menu, isOpen]);

  useEffect(() => {
    if (isOpen) {
      fetchProductList();
    }
  }, [isOpen]);

  const fetchProductList = async () => {
    setProductsLoading(true);
    try {
      const response: IResponse<IList<IProductListItem>> = await apiClient.get(
        API_END_POINTS.GET_CMS_PRODUCT_LIST +
          'status=ENABLED&offset=0&pageSize=1000',
      );

      if (isSuccessResponse(response.statusCode) && response.data?.items) {
        setProductOptions(
          response.data.items
            .map(product => {
              const id = product.productId ?? product.id ?? '';
              const name = product.productName ?? product.name ?? '';
              return {
                id,
                label: name,
                value: id,
              };
            })
            .filter(option => option.label && option.value),
        );
      }
    } catch (error) {
      console.error('Error fetching products:', error);
      toast.error('Failed to load products');
    } finally {
      setProductsLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Menu name is required';
    }

    if (!formData.url.trim()) {
      newErrors.url = 'Menu URL is required';
    } else if (!formData.url.startsWith('/')) {
      newErrors.url = 'URL must start with /';
    }

    if (!formData.icon) {
      newErrors.icon = 'Icon is required';
    }

    if (formData.actions.length === 0) {
      newErrors.actions = 'At least one action must be selected';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));

    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const handleActionChange = (actionId: string, checked: boolean) => {
    const newActions = checked
      ? [...formData.actions, actionId]
      : formData.actions.filter(a => a !== actionId);

    handleInputChange('actions', newActions);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);

    if (!validateForm()) {
      setLoading(false);
      return;
    }

    let response: IResponse<IMenu>;
    if (formData.menuType === MenuType.MAIN_MENU) {
      formData.parentMenuId = null;
    }

    try {
      if (menu) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_MENU.replace(':id', menu.id.toString()),
          { data: formData },
        );
      } else {
        formData.code = formData.name.toUpperCase().replace(/\s+/g, '_');
        response = await apiClient.post(API_END_POINTS.CREATE_MENU, {
          data: formData,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success('Menu created successfully');
      } else {
        toast.error('Failed to create menu');
      }
    } catch (error) {
      console.error('Error creating menu:', error);
      toast.error('An error occurred while saving the menu');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] max-w-2xl overflow-auto">
        <DialogHeader>
          <DialogTitle>Menu Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new menu item
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="name" className="font-medium text-foreground">
                Menu Name *
              </Label>
              <Input
                id="name"
                placeholder="e.g., Dashboard, User Management"
                value={formData.name}
                onChange={e => handleInputChange('name', e.target.value)}
                className={errors.name ? 'has-error' : ''}
              />
              {errors.name && (
                <p className="text-sm text-destructive">{errors.name}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label
                htmlFor="sequenceNumber"
                className="font-medium text-foreground"
              >
                Sequence Order *
              </Label>
              <Input
                id="sequenceNumber"
                type="number"
                min={1}
                placeholder="e.g., 1, 2, 3"
                value={formData.sequenceNumber}
                onChange={e =>
                  handleInputChange('sequenceNumber', +e.target.value)
                }
                className={errors.sequenceNumber ? 'has-error' : ''}
              />
              {errors.sequenceNumber && (
                <p className="text-sm text-destructive">
                  {errors.sequenceNumber}
                </p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="icon" className="font-medium text-foreground">
                Icon *
              </Label>
              <Select
                value={formData.icon}
                onValueChange={value => handleInputChange('icon', value)}
              >
                <SelectTrigger className={errors.icon ? 'has-error' : ''}>
                  <SelectValue placeholder="Select an icon" />
                </SelectTrigger>
                <SelectContent>
                  {SidebarMenuIcons.map(icon => (
                    <SelectItem key={icon.value.displayName} value={icon.label}>
                      <div className="flex items-center space-x-4">
                        <icon.value className="size-4 text-primary" />
                        <span>{icon.label}</span>
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.icon && (
                <p className="text-sm text-destructive">{errors.icon}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="url" className="font-medium text-foreground">
                URL *
              </Label>
              <Input
                id="url"
                placeholder="/menu-url"
                value={formData.url}
                onChange={e => handleInputChange('url', e.target.value)}
                className={errors.url ? 'has-error' : ''}
              />
              {errors.url && (
                <p className="text-sm text-destructive">{errors.url}</p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="menuType" className="font-medium text-foreground">
                Menu Type *
              </Label>
              <Select
                value={formData.menuType}
                onValueChange={value => handleInputChange('menuType', value)}
              >
                <SelectTrigger className={errors.menuType ? 'has-error' : ''}>
                  <SelectValue placeholder="Select a menu type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem
                    key={MenuType.MAIN_MENU}
                    value={MenuType.MAIN_MENU}
                  >
                    Main Menu
                  </SelectItem>
                  <SelectItem key={MenuType.SUB_MENU} value={MenuType.SUB_MENU}>
                    Sub Menu
                  </SelectItem>
                </SelectContent>
              </Select>
              {errors.menuType && (
                <p className="text-sm text-destructive">{errors.menuType}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label
                htmlFor="parentMenuId"
                className="font-medium text-foreground"
              >
                Parent Menu
              </Label>
              <Select
                value={formData.parentMenuId ?? ''}
                onValueChange={value =>
                  handleInputChange('parentMenuId', value)
                }
                disabled={formData.menuType === MenuType.MAIN_MENU}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select parent menu" />
                </SelectTrigger>
                <SelectContent>
                  {parentMenuList.length === 0 ? (
                    <SelectItem disabled value="none">
                      No Parent Menus Available
                    </SelectItem>
                  ) : (
                    parentMenuList.map(option => (
                      <SelectItem key={option.id} value={option.id}>
                        <div className="flex items-center space-x-4">
                          <span>{GetMenuIcon(option.icon)}</span>
                          <span>{option.name}</span>
                        </div>
                      </SelectItem>
                    ))
                  )}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="productIds" className="font-medium text-foreground">
              Products
            </Label>
            <CustomSelect
              name="productIds"
              data={productOptions}
              isMulti
              isSearchable
              isLoading={productsLoading}
              placeholder="Select products"
              customClassName="w-full"
              value={
                formData.productIds
                  .map(id => productOptions.find(option => option.value === id))
                  .filter(Boolean) as TMultiValue<ISelectOption>
              }
              handleChange={(
                newValue:
                  | TMultiValue<ISelectOption>
                  | TSingleValue<ISelectOption>,
              ) => {
                const ids = Array.isArray(newValue)
                  ? newValue.map(item => item.value)
                  : newValue
                    ? [(newValue as ISelectOption).value]
                    : [];
                handleInputChange('productIds', ids);
              }}
            />
          </div>

          <div className="space-y-3">
            <Label className="font-medium text-foreground">Actions *</Label>
            <div className="grid grid-cols-3 gap-4">
              {actionOptions.map(action => (
                <div key={action.id} className="flex items-center space-x-3">
                  <Checkbox
                    id={action.id}
                    checked={formData.actions.includes(action.id)}
                    onCheckedChange={checked =>
                      handleActionChange(action.id, checked as boolean)
                    }
                  />
                  <div className="space-y-1">
                    <Label
                      htmlFor={action.id}
                      className="cursor-pointer text-sm font-medium text-foreground"
                    >
                      {action.label}
                    </Label>
                    <p className="text-xs text-muted-foreground">
                      {action.description}
                    </p>
                  </div>
                </div>
              ))}
            </div>
            {errors.actions && (
              <p className="text-sm text-destructive">{errors.actions}</p>
            )}
          </div>

          <div className="flex items-center space-x-2">
            <Checkbox
              id="status"
              checked={formData.status === 'ACTIVE'}
              onCheckedChange={checked =>
                handleInputChange('status', checked ? 'ACTIVE' : 'INACTIVE')
              }
            />
            <Label
              htmlFor="status"
              className="cursor-pointer text-sm font-medium text-foreground"
            >
              Active Menu
            </Label>
          </div>

          <div className="flex items-center justify-end gap-4 pt-4">
            <Button type="button" variant="outline" onClick={onClose}>
              <X className="mr-2 size-4" />
              Cancel
            </Button>
            <Button className="text-secondary" type="submit" disabled={loading}>
              <Save className="mr-2 size-4" />
              {loading
                ? menu
                  ? 'Updating Menu...'
                  : 'Creating Menu...'
                : menu
                  ? 'Update Menu'
                  : 'Create Menu'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionMenu;
