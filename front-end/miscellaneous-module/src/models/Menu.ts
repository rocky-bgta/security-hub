export enum MenuType {
  MAIN_MENU = 'MAIN_MENU',
  SUB_MENU = 'SUB_MENU',
}

export enum MenuAction {
  VIEW = 'VIEW',
  CREATE = 'CREATE',
  EDIT = 'EDIT',
  DELETE = 'DELETE',
  EXPORT = 'EXPORT',
  PUBLISH = 'PUBLISH',
  ASSIGN = 'ASSIGN',
}

export interface IMenuPayload {
  name: string;
  code: string;
  icon: string;
  sequenceNumber: number;
  parentMenuId: string | null;
  menuType: MenuType;
  url: string;
  actions: Array<MenuAction>;
  status: string;
  productIds: Array<string>;
}

export interface IMenu extends IMenuPayload {
  id: string;
  children?: Array<IMenu>;
}

export interface IMenuWithPermissions {
  name: string;
  menuId: string;
  menuCode: string;
  sequenceNumber: number;
  parentMenuId: string | null;
  menuType: MenuType;
  url: string;
  actions: Array<{
    action: MenuAction;
    selected: boolean;
  }>;
  isActive: boolean;
  children?: Array<IMenuWithPermissions>;
}
