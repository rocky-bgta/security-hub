import { Status } from 'models/Global';
import { ISelectFeature } from './Feature';
import { IProduct } from './Product';

export interface IPackage {
  id: string;
  packageName: string;
  packageDescription: string;
  price: number;
  yearlyPrice: number;
  features: { id: string; name: string }[];
  packages: any[];
  thumbnailUrl: string;
  createdAt: string;
  updatedAt: string;
  packageStatus: Status;
  courseIds: any[];
  bundlesIds: any[];
  rangePricingResponse: Array<{
    userRangeId: string;
    rangeName: string;
    pricePerUser: number;
    minUsers: number;
    maxUsers: number;
    yearlyPricePerUser: number;
  }>;
  basePackageId: string;
  productId: string;
  isTrial: boolean;
  showInSite: boolean;
}

export interface INewPackageData {
  id: string;
  packageName: string;
  packagePrice: number;
  packageYearlyPrice: number;
  selectedFeatures: Array<ISelectFeature>;
  packageStatus: Status;
  isTrial: boolean;
  showInSite: boolean;
  rangePricing: Array<{
    userRangeId: string;
    pricePerUser: number;
    yearlyPricePerUser: number;
  }>;
}

export interface IUserPackage {
  packageId: string;
  packageName: string;
  assignedDate: string;
  totalCourses: number;
  completedCourses: number;
  progress: number;
  validity: string;
  expireDate: string;
  expired: boolean;
}

export interface IUserExamQuestion {
  questionId: string;
  text: string;
  questionType: string;
  options: string[];
}

export interface IUserExam {
  id: string;
  title: string;
  passingScore: number;
  questions: IUserExamQuestion[];
}

export interface IUserTopic {
  courseId: string;
  courseName: string;
}

export interface IUserPackageDetails {
  name: string;
  packageId: string;
  packageName: string;
  validity: string;
  expireDate: string;
  status: string;
  progress: number;
  examCompleted: boolean;
  examScore: number;
  examPassed: boolean;
  correctAnswers: number;
  incorrectAnswers: number;
  examCompletedAt: string | null;
  examAttempts: number;
  completedCourses: IUserTopic[];
  exam: IUserExam;
  certificateLink: string;
  imageCertificateLink: string;
}

export enum UserSubPackageStatus {
  ASSIGNED = 'ASSIGNED',
  NOT_COMPLETED = 'NOT_COMPLETED',
  COMPLETED = 'COMPLETED',
  EXAM = 'EXAM',
  PHISHING_TRAINING_COMPLETED = 'PHISHING_TRAINING_COMPLETED',
}

export interface IUserSubPackage {
  assignedAt: string;
  expiryDate: string;
  productId: string;
  productName: string;
  progress: number;
  status: UserSubPackageStatus;
  subPackageId: string;
  subPackageName: string;
  topicCount: number;
}

export interface IUserSubPackageTopic {
  isBookmarked: boolean;
  chapterIds: Array<{
    chapterName: string;
    id: string;
  }>;
  description: string;
  durationMinutes: number;
  id: string;
  status: string;
  thumbnailUrl: string;
  topicName: string;
  topicProgress: number;
  totalContentCount: number;
  subPackageValidity: string;
  subPackageId: string;
}

export interface IPackageDetails {
  id: string;
  product: IProduct;
  packageDetails: IPackage;
}
