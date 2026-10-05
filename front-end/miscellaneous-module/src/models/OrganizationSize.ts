export interface IOrganizationSizePayload {
  name: string;
  range: string;
}

export interface IOrganizationSize extends IOrganizationSizePayload {
  id: string;
  createdAt: string;
  updatedAt: string;
}
