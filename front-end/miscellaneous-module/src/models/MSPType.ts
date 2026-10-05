export interface IMSPTypePayload {
  name: string;
  isActive: boolean;
}

export interface IMSPType extends IMSPTypePayload {
  id: string;
  name: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}
