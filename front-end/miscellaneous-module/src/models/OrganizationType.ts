export interface IOrganizationTypePayload {
  id?: string;
  organizationType: string;
}

export interface IOrganizationType extends IOrganizationTypePayload {
  id: string;
  createdAt: string;
  updatedAt: string;
}
