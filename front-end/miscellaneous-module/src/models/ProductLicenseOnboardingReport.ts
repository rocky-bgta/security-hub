export interface IProductLicenseOnboardingReport {
  totalLicenses: number;
  activeUsers: number;
  pendingActivation: number;
  assignedLicenses: number;
  unusedLicenses: number;
  suspendedAccounts: number;
  accountCreated: number;
  accountActivated: number;
  trainingAssigned: number;
  trainingCompleted: number;
  certificateEarned: number;
}
