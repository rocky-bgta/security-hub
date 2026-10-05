export type MspProductStatus = 'ENABLED' | 'DISABLED';

/** Package shape returned by GET /msp/{mspId}/products/catalog */
export interface IMspCatalogPackage {
  packageId: string;
  packageName: string;
  price: number;
  packageStatus: string;
  mspProductId: string | null;
  licenseStatus: string | null;
}

/** Product shape returned by GET /msp/{mspId}/products/catalog */
export interface IMspCatalogProduct {
  productId: string;
  productName: string;
  productDescription: string;
  thumbnailUrl: string | null;
  displayOrder: number;
  mspProductStatus: MspProductStatus;
  mspProductId: string | null;
  licenseStatus: string | null;
  packages: IMspCatalogPackage[];
}

export interface IContactSalesPayload {
  productId: string;
  email: string;
  phone: string;
  firstName: string;
  lastName: string;
  companyName: string;
  numberOfEmployees: string;
  hearAboutUs: string;
}

export interface IPreselectedProduct {
  productId: string;
  productName: string;
  productDescription: string;
}
