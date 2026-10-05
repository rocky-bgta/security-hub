export interface VatRegion {
  id: string;
  regionName: string;
  vatRate: number;
}

export interface VatConfiguration {
  id: string;
  countryName: string;
  defaultVatRate: number;
  regionBased: boolean;
  regions: VatRegion[];
  createdAt: number;
  updatedAt: number;
}

export interface VatFormData {
  id: string;
  countryName: string;
  defaultVatRate: number;
  regionBased: boolean;
  regions: VatRegion[];
}

export interface VatApiResponse {
  error: boolean;
  message: string;
  statusCode: number;
  data: VatConfiguration[];
  total: number;
  path: string | null;
  timestamp: string;
}
