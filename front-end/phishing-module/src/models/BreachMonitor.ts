export type Severity = 'critical' | 'high' | 'medium' | 'low' | 'info';
export type ThreatStatus = 'open' | 'in_mitigation' | 'completed';
export type BreachType = 'email' | 'ip' | 'credential' | 'domain';
export type MonitoredAssetType = 'email' | 'ip' | 'domain' | 'keyword';

export interface BreachAlert {
  id: string;
  email?: string;
  ip?: string;
  passwordHash: string;
  source: string;
  databaseName: string;
  domain: string;
  breachDate: string;
  detectedDate: string;
  severity: Severity;
  type: BreachType;
  status: ThreatStatus;
  echoes?: number;
  foundIn?: string;
  ingestionDate?: string;
}

export interface IPBreachAlert {
  id: string;
  fakeDomain: string;
  type: string;
  dateDetected: string;
  severity: Severity;
  status: ThreatStatus;
  source: string;
  recommendation: string;
}

export interface ThreatIntelAlert {
  id: string;
  threatDomain: string;
  description: string;
  dateFound: string;
  severity: Severity;
  status: ThreatStatus;
  category: 'credential_leak' | 'ransomware' | 'data_leak' | 'malware';
  source: string;
  recommendation: string;
}

export interface ThreatTypeData {
  name: string;
  value: number;
  color: string;
}

export interface TacticDistribution {
  name: string;
  value: number;
  color: string;
}

export interface MonitoredAsset {
  id: string;
  value: string;
  type: MonitoredAssetType;
  totalBreaches: number;
  lastBreachDate: string | null;
  addedDate: string;
  isActive: boolean;
}

export interface ThreatIntelSummary {
  open: number;
  inMitigation: number;
  completed: number;
}

export interface BreachActivityPoint {
  date: string;
  critical: number;
  high: number;
  medium: number;
  low: number;
}

export interface Recommendation {
  id: string;
  title: string;
  description: string;
  severity: Severity;
  breachId: string;
  actionType:
    | 'change_password'
    | 'enable_2fa'
    | 'rotate_key'
    | 'block_ip'
    | 'notify_user';
  isCompleted: boolean;
}

export interface DashboardStats {
  totalEmailBreaches: number;
  totalIPBreaches: number;
  monitoredEmails: number;
  monitoredIPs: number;
  threatIntel: ThreatIntelSummary;
}

export interface BreachFilter {
  search: string;
  severity: Severity | 'all';
  type: BreachType | 'all';
  status: ThreatStatus | 'all';
  source: string;
  dateRange: { from: string; to: string } | null;
}

export type VulnStatus = 'unpatched' | 'in_progress' | 'open' | 'patched';

export interface VulnerabilityAlert {
  id: string;
  cveId: string;
  vulnerability: string;
  riskLevel: Severity;
  status: VulnStatus;
  description: string;
  affectedSystem: string;
  dateDetected: string;
  recommendation: string;
}

export interface RiskDistData {
  name: string;
  value: number;
  color: string;
}

export interface ImpersonationAlert {
  id: string;
  fakeDomain: string;
  type:
    | 'Phishing Site'
    | 'Botnet Log'
    | 'Exploit Database'
    | 'Malicious Clone'
    | 'Spoofed Email'
    | 'Typosquatting';
  dateDetected: string;
  severity: Severity;
  status: ThreatStatus | 'resolved';
  source: string;
  recommendation: string;
  description?: string;
}

export const severityClasses: Record<Severity, string> = {
  critical: 'border border-[#dc2626]/30 bg-[#dc2626]/15 text-[#dc2626]',
  high: 'border border-[#f97316]/30 bg-[#f97316]/15 text-[#f97316]',
  medium: 'border border-[#eab308]/30 bg-[#eab308]/15 text-[#eab308]',
  low: 'border border-[#22c55e]/30 bg-[#22c55e]/15 text-[#22c55e]',
  info: 'border border-[#3b82f6]/30 bg-[#3b82f6]/15 text-[#3b82f6]',
};

export const statusColors: Record<ThreatStatus, string> = {
  open: 'border border-[#dc2626]/30 bg-[#dc2626]/15 text-[#dc2626]',
  in_mitigation: 'border border-[#eab308]/30 bg-[#eab308]/15 text-[#eab308]',
  completed: 'border border-[#22c55e]/30 bg-[#22c55e]/15 text-[#22c55e]',
};

export interface IBreachedEmailResponse {
  id: string;
  externalFindingId: string;
  timestamp: string;
  domain: string;
  email: string;
  ipAddress: string;
  username: string;
  phone: string;
  databaseName: string;
  foundIn: string;
  source: string;
  leakName: string;
  password: string;
  breachDescription: string;
  compromisedData: string;
  breachSeverity: Severity;
  victimDomain: string;
  breachStatus: string;
  employee: boolean;
  echoesCount: number;
  firstSeenAt: string;
  lastSeenAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface IBreachEmailActivityResponse {
  days: number;
  from: string;
  to: string;
  buckets: Array<BreachActivityPoint>;
  totalCritical: number;
  totalHigh: number;
  totalMedium: number;
  totalLow: number;
  total: number;
}

export interface BreachEmailSummaryResponse {
  domainCount: number;
  breachCount: number;
}
