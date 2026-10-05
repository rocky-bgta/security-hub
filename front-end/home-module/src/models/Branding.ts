export interface IBrandingResponse {
  id: string;
  companyName: string;
  logoFilePath: string;
  clientAdminId: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  active: boolean;
}

export interface IBrandingData {
  companyName: string;
  logoFilePath: string;
}
