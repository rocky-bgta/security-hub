export interface IDepartmentPayload {
  name: string;
  description: string;
  isSystemDefined: boolean;
  active: boolean;
}

export interface IDepartment extends IDepartmentPayload {
  id: string;
  name: string;
  description: string;
  clientAdminId: string;
  isSystemDefined: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}
