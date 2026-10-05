export interface IUserRangePayload {
  rangeName: string;
  minUsers: number;
  maxUsers: number;
  description: string;
  isDefault: boolean;
}

export interface IUserRange extends IUserRangePayload {
  id: string;
  rangeName: string;
  minUsers: number;
  maxUsers: number;
  description: string;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
}
