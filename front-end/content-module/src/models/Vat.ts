export interface IVatRegion {
  id: string;
  regionName: string;
  vatRate: number;
}

export interface IVatConfiguration {
  id: string;
  countryName: string;
  defaultVatRate: number;
  regionBased: boolean;
  regions: IVatRegion[];
  createdAt: number;
  updatedAt: number;
}
