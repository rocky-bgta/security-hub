export interface INetTermPayload {
  netTermName: string;
  netTermInDays: number;
  isActive: boolean;
}

export interface INetTerm extends INetTermPayload {
  id: string;
  netTermName: string;
  netTermInDays: number;
  isActive: boolean;
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
}
