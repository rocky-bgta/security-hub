/**
 * Breach Detection TypeScript Models
 */

// Enums
export enum BreachSeverity {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
}

export enum BreachStatus {
  ACTION_REQUIRED = 'ACTION_REQUIRED',
  IN_PROGRESS = 'IN_PROGRESS',
  RESOLVED = 'RESOLVED',
}

export enum RecipientBreachStatus {
  PENDING = 'PENDING',
  NOTIFIED = 'NOTIFIED',
  RESOLVED = 'RESOLVED',
}

// Breach Record Interface
export interface IBreachRecord {
  id: string;
  domain: string;
  breachName: string;
  dateOfBreach: string;
  description: string;
  compromisedDataTypes: string[];
  recipientCount: number;
  severity: BreachSeverity;
  status: BreachStatus;
  sourceApi: string;
  externalBreachId: string;
  createdAt: string;
  updatedAt: string;
  severityLabel: string;
  statusLabel: string;
  compromisedDataSummary: string;
}

// Action Log Interface
export interface IActionLog {
  action: string;
  actionLabel: string;
  performedBy: string;
  performedAt: string;
  notes: string;
}

// Recipient Breach Interface
export interface IRecipientBreach {
  id: string;
  breachRecordId: string;
  breachName?: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  tags: string[];
  breachCount: number;
  status: RecipientBreachStatus;
  notifiedAt: string | null;
  passwordResetAt: string | null;
  resolvedAt: string | null;
  actionLogs: IActionLog[];
  createdAt: string;
  statusLabel: string;
  canNotify: boolean;
  canResetPassword: boolean;
  canResolve: boolean;
}

// Breach Configuration Interface
export interface IBreachConfig {
  id: string;
  collectBreachData: boolean;
  monitoredDomains: string[];
  autoNotifyUsers: boolean;
  requirePasswordReset: boolean;
  syncIntervalHours: number;
  lastSyncAt: string | null;
  updatedAt: string | null;
  updatedBy: string | null;
}

// Sync Result Interface
export interface IBreachSyncResult {
  domainsProcessed: number;
  newBreachesFound: number;
  newRecipientsFound: number;
  errorsEncountered: number;
  syncStartedAt: string;
  syncCompletedAt: string;
  durationMs: number;
  status: string;
  message: string;
}

// Request DTOs
export interface IBreachStatusRequest {
  status: BreachStatus;
  notes?: string;
}

export interface IRecipientActionRequest {
  notes?: string;
  sendEmail?: boolean;
  customMessage?: string;
}

export interface IBreachConfigRequest {
  collectBreachData: boolean;
  monitoredDomains: string[];
  autoNotifyUsers: boolean;
  requirePasswordReset: boolean;
  syncIntervalHours: number;
}

// Helper Functions
export const getSeverityColor = (severity: BreachSeverity): string => {
  switch (severity) {
    case BreachSeverity.HIGH:
      return '#EF4444'; // Red
    case BreachSeverity.MEDIUM:
      return '#F59E0B'; // Amber
    case BreachSeverity.LOW:
      return '#10B981'; // Green
    default:
      return '#6B7280'; // Gray
  }
};

export const getSeverityBgColor = (severity: BreachSeverity): string => {
  switch (severity) {
    case BreachSeverity.HIGH:
      return '#FEE2E2'; // Red light
    case BreachSeverity.MEDIUM:
      return '#FEF3C7'; // Amber light
    case BreachSeverity.LOW:
      return '#D1FAE5'; // Green light
    default:
      return '#F3F4F6'; // Gray light
  }
};

export const getStatusColor = (status: BreachStatus): string => {
  switch (status) {
    case BreachStatus.ACTION_REQUIRED:
      return '#EF4444'; // Red
    case BreachStatus.IN_PROGRESS:
      return '#F59E0B'; // Amber
    case BreachStatus.RESOLVED:
      return '#10B981'; // Green
    default:
      return '#6B7280'; // Gray
  }
};

export const getRecipientStatusColor = (
  status: RecipientBreachStatus,
): string => {
  switch (status) {
    case RecipientBreachStatus.PENDING:
      return '#EF4444'; // Red
    case RecipientBreachStatus.NOTIFIED:
      return '#3B82F6'; // Blue
    case RecipientBreachStatus.RESOLVED:
      return '#10B981'; // Green
    default:
      return '#6B7280'; // Gray
  }
};

export const getSeverityLabel = (severity: BreachSeverity): string => {
  switch (severity) {
    case BreachSeverity.HIGH:
      return 'High';
    case BreachSeverity.MEDIUM:
      return 'Medium';
    case BreachSeverity.LOW:
      return 'Low';
    default:
      return 'Unknown';
  }
};

export const getStatusLabel = (status: BreachStatus): string => {
  switch (status) {
    case BreachStatus.ACTION_REQUIRED:
      return 'Action Required';
    case BreachStatus.IN_PROGRESS:
      return 'In Progress';
    case BreachStatus.RESOLVED:
      return 'Resolved';
    default:
      return 'Unknown';
  }
};

export const getRecipientStatusLabel = (
  status: RecipientBreachStatus,
): string => {
  switch (status) {
    case RecipientBreachStatus.PENDING:
      return 'Pending';
    case RecipientBreachStatus.NOTIFIED:
      return 'Notified';
    case RecipientBreachStatus.RESOLVED:
      return 'Resolved';
    default:
      return 'Unknown';
  }
};

// Compromised data type icons
export const getCompromisedDataIcon = (dataType: string): string => {
  const type = dataType.toLowerCase();
  if (type.includes('password')) return '🔐';
  if (type.includes('email')) return '📧';
  if (type.includes('phone')) return '📱';
  if (type.includes('address')) return '📍';
  if (type.includes('credit') || type.includes('card')) return '💳';
  if (type.includes('ssn') || type.includes('social')) return '🔢';
  return '📄';
};
