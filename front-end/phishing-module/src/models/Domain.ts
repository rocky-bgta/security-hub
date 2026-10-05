/**
 * Domain entity model
 * Based on Task-01 Domain Verification
 */

export enum DomainStatus {
  UNVERIFIED = 'UNVERIFIED',
  VERIFIED = 'VERIFIED',
  VERIFIED_AND_LOCKED = 'VERIFIED_AND_LOCKED',
}

export interface IDomain {
  domainId: string;
  domain: string;
  status: DomainStatus;
  isLocked: boolean;
  canLock: boolean;
  lockedByTenantId: string | null;
  verifiedBy: string | null;
  lockedBy: string | null;
  verifiedAt: string | null;
  lockedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface IDomainListResponse {
  offset: number;
  pageSize: number;
  total: number;
  items: IDomain[];
}

/**
 * Get display text for domain status
 */
export const getDomainStatusLabel = (status: DomainStatus): string => {
  switch (status) {
    case DomainStatus.UNVERIFIED:
      return 'Unverified';
    case DomainStatus.VERIFIED:
      return 'Verified';
    case DomainStatus.VERIFIED_AND_LOCKED:
      return 'Verified & Locked';
    default:
      return status;
  }
};

/**
 * Get badge variant for domain status
 */
export const getDomainStatusVariant = (
  status: DomainStatus,
): 'default' | 'secondary' | 'destructive' | 'outline' => {
  switch (status) {
    case DomainStatus.UNVERIFIED:
      return 'secondary';
    case DomainStatus.VERIFIED:
      return 'default';
    case DomainStatus.VERIFIED_AND_LOCKED:
      return 'outline';
    default:
      return 'secondary';
  }
};
