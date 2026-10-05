export interface IUserCertificate {
  courseId: string;
  courseName: string;
  certificateLink: string;
  createdAt: string;
  thumbnailUrl: string;
  imageCertificateLink: string;
}

export interface IUserCertificateStatistics {
  total: number;
  validCount: number;
  expiredCount: number;
  expiringSoonCount: number;
  notCompleteCount: number;
}
