export interface IRolePayload {
  roleName: string;
  description: string;
  accessLevel: number;
  colorTheme: string;
  status: string;
}

export interface IRole extends IRolePayload {
  id: string;
  userCount: number;
  createdAt?: string;
  updatedAt?: string;
}
