/** Minimal product shape from assigned-license API */
export interface IAssignedProductRef {
  productId: string;
  productName: string;
  displayOrder: number;
  tags: string[];
}

export interface IAssignedLicense {
  id: string;
  clientAdminId: string | null;
  productId: string;
  packageId: string;
  product: IAssignedProductRef;
  packageDetails: {
    id: string;
    packageName: string;
    packageStatus: string;
  };
  licenseCount: number;
  usedLicenseCount: number;
  assignedAt: string;
  expiryDate: string | null;
  topicCount: number;
}
