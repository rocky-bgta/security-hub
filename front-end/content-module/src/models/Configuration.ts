export interface ICategory {
  id: string;
  categoryName: string;
  description: string;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  active: boolean;
}

export interface IContentType {
  id: string;
  typeName: string;
  description: string;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface ICompliance {
  id: string;
  complianceName: string;
  acronym: string;
  description: string;
  isActive: boolean;
  sortOrder: number;
  createdAt: string; // or Date
  updatedAt: string; // or Date
}

export interface ICountry {
  id: string;
  name: string;
  countryCode: string;
  sortOrder: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ITag {
  id: string;
  name: string;
  description: string;
  status: string;
  createdAt: string;
  createdBy: string;
}
