import { Status } from './Global';

export interface ProductPackage {
  productId: string;
  productName: string;
  packageIds: string[];
}

export interface ITopic {
  id: string;
  topicName: string;
  categoryIds: string[];
  countryIds: string[];
  complianceIds: string[];
  contentTypeId: string;
  durationMinutes: number;
  description: string;
  thumbnailUrl: string;
  productPackages: ProductPackage[];
  chapterIds: string[];
  totalContentCount: number | null;
  createdBy: string | null;
  status: Status;
  createdAt: string;
  updatedAt: string;
}

export interface ITopicResponse {
  topics: ITopic[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface IMspTopic {
  topicId: string;
  topicName: string;
  description: string;
  durationMinutes: number;
  thumbnail: string;
  contentType: string;
  category: string[];
}
