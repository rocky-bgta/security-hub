import ROLE from 'utils/Role';

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

export interface ISideMenu {
  code: string;
  icon: string;
  id: string;
  menuType: MenuType;
  name: string;
  parentMenuId: string | null;
  permissions: Array<string>;
  permittedActions: Array<MenuAction> | null;
  sequenceNumber: number;
  url: string;

  subItems: Array<ISideMenu>;
}

export interface ISideMenuResponse {
  roleId: string;
  roleName: ROLE;
  userMenuResponses: Array<ISideMenu>;
}

export interface INavigationItem {
  icon: string;
  name: string;
  subItems: Array<INavigationItem>;
  url: string;
}
