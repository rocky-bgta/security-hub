import { Status } from 'models/Global';
import { ContentTypes } from './Content';
import { IPackage } from './Package';

export interface ICourse {
  id: string;
  courseName: string;
  courseDescription: string;
  courseStatus: Status;
  productIds: any[];
  chapterIds: any[];
  createdAt: string;
  updatedAt: string;
  thumbnailUrl?: string;
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
  productPackages: IPackage[];
  chapterIds: string[];
  totalContentCount: number | null;
  createdBy: string | null;
  status: Status;
  createdAt: string;
  updatedAt: string;
}

export interface ICategoryDetail {
  id: string;
  categoryName: string;
  description: string;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  active: boolean;
}

export interface ICountryDetail {
  id: string;
  countryName: string;
  countryCode: string;
  sortOrder: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface IComplianceDetail {
  id: string;
  complianceName: string;
  acronym: string;
  description: string;
  isActive: boolean;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface IContentTypeDetail {
  id: string;
  typeName: string;
  description: string;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface IProductPackage {
  productId: string;
  productName: string;
  packageIds: string[];
  packageDetails: [{ name: string; price: string }];
}

export interface ITopicDetails {
  id: string;
  topicName: string;
  description: string;
  status: 'ENABLED' | 'DISABLED';
  chapterIds: string[];
  productPackages: IProductPackage[];
  thumbnailUrl: string;
  durationMinutes: number;
  totalContentCount: number;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  categoryDetails: ICategoryDetail[];
  countryDetails: ICountryDetail[];
  complianceDetails: IComplianceDetail[];
  contentTypeDetails: IContentTypeDetail;
}

export interface IUserCourse {
  topicId: string;
  courseName: string;
  chapterCount: number;
  totalContentCount: number;
  completedContentCount: number;
  progress: number;
  status: string;
  certificateLink: string;
  expired: boolean;
  thumbnailUrl: string;
  saved: boolean;
  createdAt: string;
}

export interface IUserChapter {
  chapterId: string;
  chapterName: string;
  chapterDescription: string;
  contents: [
    {
      contentId: string;
      title: string;
      contentType: ContentTypes;
      done: boolean;
      contentList: [
        {
          id: string;
          contentName: string;
        },
      ];
    },
  ];
}

export interface IUserCourseDetails {
  topicId: string;
  topicName: string;
  topicDescription: string;
  publishDate: string;
  progress: number;
  chapterCount: number;
  contentCount: number;
  chapters: IUserChapter[];
  thumbnailUrl: string;
  certificateUrl: string;
  status: string;
  durationMinutes: number;
  categoryIds: string[];
  countryIds: string[];
  complianceIds: string[];
  contentTypeId: string;
  imageCertificateLink: string;
  saved: boolean;
}

export interface IUserDashboardSummary {
  completed: number;
  exam: number;
  inProgress: number;
  notStarted: number;
  total: number;
}

export interface ITopicsStatistics {
  subPackageId: string;
  subPackageName: string;
  total: number;
  completed: number;
  pending: number;
  inProgress: number;
}

export interface IDashboardCardTooltip {
  id: string;
  packageName: string;
  price: number;
  thumbnailUrl: string;
}

export interface IDashboardCardTooltipGrouped {
  groupedPackages: {
    IN_PROGRESS: IDashboardCardTooltip[];
    NOT_STARTED: IDashboardCardTooltip[];
    COMPLETED: IDashboardCardTooltip[];
  };
}

export interface ISelectTopic {
  id: string;
  topicName: string;
}
