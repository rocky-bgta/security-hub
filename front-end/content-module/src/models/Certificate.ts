import { Status } from './Global';

export interface IUserCertificate {
  productId: string;
  productName: string;
  thumbnailUrl: string | null;
  certificateLink: string;
  imageCertificateLink: string;
  createdAt: string;
}

export interface IUserCertificateStatistics {
  validCount: number;
  expiredCount: number;
  expiringSoonCount: number;
  notCompleteCount: number;
}

export interface ICertificateHistory {
  certificateId: string;
  fullName: string;
  email: string;
  userId: string;
  productName: string;
  subPackageId: string;
  certificateUrl: string;
  certificateImageUrl: string;
  status: string;
  expiryDate: string;
  createdAt: string;
}

export interface ICertificateTemplate {
  id: string;
  templateName: string;
  certificateTitle: string;
  certificateType: string;
  acknowledgement: string;
  completionStatus: string;
  completionTitle: string;
  logoImageUrl: string;
  signatureImageUrl: string;
  signerName: string;
  signerDesignation: string;
  signatureIdentity: string;
  backgroundImageUrl: string;
  dynamicFields: {
    showLearnerName: boolean;
    showCourseName: boolean;
    showIssueDate: boolean;
    showCertificateId: boolean;
    showQrCode: boolean;
  };
  status: string;
  createdAt?: string;
  updatedAt?: string;
  isDefault: boolean;
  isTrialTemplate: boolean;
}

export interface IClientCertificateTemplate {
  id: string;
  templateName: string;
  certificateTitle: string;
  certificateType: string;
  acknowledgement: string;
  completionStatus: string;
  completionTitle: string;
  logoImageUrl: string;
  signatureImageUrl: string;
  signerName: string;
  signerDesignation: string;
  signatureIdentity: string;
  backgroundImageUrl: string;
  dynamicFields: {
    showLearnerName: boolean;
    showCourseName: boolean;
    showIssueDate: boolean;
    showCertificateId: boolean;
    showQrCode: boolean;
  };
  status: Status;
  isDefault: boolean;
  isTrialTemplate: boolean;
  isAssigned: boolean;
}

export interface ICertificateTemplateDetails {
  id: string;
  templateName: string;
  certificateTitle: string;
  certificateType: string;
  acknowledgement: string;
  completionStatus: string;
  completionTitle: string;
  logoImageUrl: string;
  signatureImageUrl: string;
  signerName: string;
  signerDesignation: string;
  signatureIdentity: string;
  backgroundImageUrl: string;
  dynamicFields: {
    showLearnerName: boolean;
    showCourseName: boolean;
    showIssueDate: boolean;
    showCertificateId: boolean;
    showQrCode: boolean;
  };
  status: Status;
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
  isDefault: boolean;
  isTrialTemplate: boolean;
}

export interface IExpiringCertificate {
  learnerName: string;
  clientAdminId: string;
  clientAdminName: string;
  courseName: string;
  certificateId: string;
  expiryDate: string;
  status: string;
}

export interface ICertificateSummaryStats {
  totalCertificatesIssued: number;
  activeCertificatesCount: number;
  averageCompletionRate: number;
  expiringThisMonth: number;
}
