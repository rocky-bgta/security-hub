export interface IDropdownItem {
  id: string;
  name: string;
  description?: string;
  displayOrder?: number;
  isDefault?: boolean;
  isActive?: boolean;
}
