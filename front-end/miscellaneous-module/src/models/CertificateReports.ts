import { IList } from 'models/Global';

export type TCertificateReportStatus = 'VALID' | 'EXPIRING_SOON' | 'EXPIRED';

export interface IIssuedCertificateReportSummary {
  totalIssued: number;
  thisMonth: number;
  thisQuarter: number;
}

export interface IIssuedCertificateReportRow {
  certificateId: string;
  user: string;
  course: string;
  issued: string;
  expiry: string;
  issuedBy: string;
  status: string;
}

export interface IExpiredCertificateReportSummary {
  totalCertificates: number;
  totalValidCertificates: number;
  totalExpiredCertificates: number;
  totalExpiringCertificates: number;
}

export interface IExpiredCertificateReportRow {
  certificateId: string;
  user: string;
  course: string;
  expiryDate: string;
  days: string;
  certificateStatus: string;
}

export type IIssuedCertificateReportList = IList<IIssuedCertificateReportRow>;
export type IExpiredCertificateReportList = IList<IExpiredCertificateReportRow>;
