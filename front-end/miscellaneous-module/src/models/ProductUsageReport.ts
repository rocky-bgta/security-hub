export interface IProductUtilizationItem {
  id: string;
  productId: string;
  packageId: string;
  productName: string;
  totalUsers: number;
  totalLicenseCount: number;
  utilizationPercentage: number;
}

export interface IProductUsageReportData {
  clientAdminId: string;
  totalProduct: number;
  totalActiveModule: number;
  totalInteraction: number;
  averageEngagement: number;
  products: IProductUtilizationItem[];
}
