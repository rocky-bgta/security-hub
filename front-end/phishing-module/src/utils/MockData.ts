import type {
  BreachActivityPoint,
  BreachAlert,
  DashboardStats,
  ImpersonationAlert,
  IPBreachAlert,
  MonitoredAsset,
  Recommendation,
  RiskDistData,
  TacticDistribution,
  ThreatIntelAlert,
  ThreatTypeData,
  VulnerabilityAlert,
} from 'models/BreachMonitor';

import { MOCK_DEMO_IPS, mockIpBlockDescription } from './mockDemoIps';

export const mockDashboardStats: DashboardStats = {
  totalEmailBreaches: 7,
  totalIPBreaches: 3,
  monitoredEmails: 15,
  monitoredIPs: 9,
  threatIntel: { open: 300, inMitigation: 12, completed: 8 },
};

export const mockEmailBreaches: BreachAlert[] = [
  {
    id: 'eb-1',
    email: 'aminul.haque@ucb.com.bd',
    passwordHash: '******2',
    source: 'dark web forum',
    databaseName: '500mb_email_pass_fresh_0204',
    domain: 'United Commercial Bank (UCB)',
    breachDate: '2026-04-01',
    detectedDate: '2026-04-12',
    ingestionDate: '2026-04-12',
    severity: 'critical',
    type: 'email',
    status: 'open',
    echoes: 0,
    foundIn: 'cracked.ax - Beyond the Limits',
  },
  {
    id: 'eb-2',
    email: 'saykat.kundu@ucb.com.bd',
    passwordHash: '**********1',
    source: 'dark web API',
    databaseName: 'alien txtbase',
    domain: 'United Commercial Bank (UCB)',
    breachDate: '2025-10-05',
    detectedDate: '2026-04-12',
    ingestionDate: '2026-04-12',
    severity: 'high',
    type: 'email',
    status: 'open',
    echoes: 2,
    foundIn: 'multiple sources',
  },
  {
    id: 'eb-3',
    email: 'jahidul.islam@ucb.com.bd',
    passwordHash: '************D',
    source: 'dark web API',
    databaseName: 'chegg',
    domain: 'United Commercial Bank (UCB)',
    breachDate: '2018-04-01',
    detectedDate: '2026-04-12',
    ingestionDate: '2026-04-12',
    severity: 'medium',
    type: 'email',
    status: 'completed',
    echoes: 1,
    foundIn: 'multiple sources',
  },
  {
    id: 'eb-4',
    email: 'security@bigcorp.net',
    passwordHash: '*****3b',
    source: 'dark web forum',
    databaseName: 'mega_leak_2024',
    domain: 'BigCorp Networks',
    breachDate: '2025-01-05',
    detectedDate: '2025-01-10',
    ingestionDate: '2025-01-10',
    severity: 'critical',
    type: 'email',
    status: 'open',
    echoes: 0,
    foundIn: 'cracked.ax - Beyond the Limits',
  },
  {
    id: 'eb-5',
    email: 'ops@startup.dev',
    passwordHash: '*****cc',
    source: 'Alinet',
    databaseName: 'dev_credentials_2025',
    domain: 'Startup Dev Inc.',
    breachDate: '2025-02-14',
    detectedDate: '2025-02-20',
    ingestionDate: '2025-02-20',
    severity: 'high',
    type: 'email',
    status: 'open',
    echoes: 3,
    foundIn: 'multiple sources',
  },
  {
    id: 'eb-6',
    email: 'hr@enterprise.org',
    passwordHash: '*****7d',
    source: 'Alinet',
    databaseName: 'hr_systems_dump',
    domain: 'Enterprise Org',
    breachDate: '2024-09-05',
    detectedDate: '2024-09-12',
    ingestionDate: '2024-09-12',
    severity: 'low',
    type: 'email',
    status: 'completed',
    echoes: 0,
    foundIn: 'Alinet',
  },
  {
    id: 'eb-7',
    email: 'cto@fintech.com',
    passwordHash: '*****e9',
    source: 'dark web API',
    databaseName: 'fintech_breach_2025',
    domain: 'FinTech Corp',
    breachDate: '2025-03-01',
    detectedDate: '2025-03-05',
    ingestionDate: '2025-03-05',
    severity: 'critical',
    type: 'email',
    status: 'open',
    echoes: 1,
    foundIn: 'dark web API',
  },
];

export const mockIPBreaches: BreachAlert[] = [
  {
    id: 'ib-1',
    ip: MOCK_DEMO_IPS.INTERNAL_LAN,
    passwordHash: 'N/A',
    source: 'Alinet',
    databaseName: 'ip_exposure_scan',
    domain: 'internal',
    breachDate: '2025-01-20',
    detectedDate: '2025-01-25',
    severity: 'high',
    type: 'ip',
    status: 'open',
  },
  {
    id: 'ib-2',
    ip: MOCK_DEMO_IPS.PRIVATE_NETWORK,
    passwordHash: 'N/A',
    source: 'dark web API',
    databaseName: 'network_scan_dump',
    domain: 'internal',
    breachDate: '2024-12-10',
    detectedDate: '2024-12-15',
    severity: 'medium',
    type: 'ip',
    status: 'in_mitigation',
  },
  {
    id: 'ib-3',
    ip: MOCK_DEMO_IPS.DOCUMENTATION,
    passwordHash: 'N/A',
    source: 'dark web forum',
    databaseName: 'public_ip_list_2025',
    domain: 'external',
    breachDate: '2025-02-28',
    detectedDate: '2025-03-02',
    severity: 'critical',
    type: 'ip',
    status: 'open',
  },
];

export const mockIPBreachAlerts: IPBreachAlert[] = [
  {
    id: 'ipa-1',
    fakeDomain: MOCK_DEMO_IPS.FAKE_DOMAIN_A,
    type: 'Phishing Site',
    dateDetected: '2026-04-12',
    severity: 'high',
    status: 'open',
    source: 'dark web API',
    recommendation: 'Report to Registrar | Enable DMARC',
  },
  {
    id: 'ipa-2',
    fakeDomain: MOCK_DEMO_IPS.FAKE_DOMAIN_B,
    type: 'Botnet Log',
    dateDetected: '2026-04-14',
    severity: 'high',
    status: 'open',
    source: 'Alinet',
    recommendation: 'Block IP | Escalate',
  },
  {
    id: 'ipa-3',
    fakeDomain: MOCK_DEMO_IPS.FAKE_DOMAIN_C,
    type: 'Exploit Database',
    dateDetected: '2026-04-08',
    severity: 'medium',
    status: 'open',
    source: 'dark web forum',
    recommendation: 'Apply Patch | Monitor',
  },
  {
    id: 'ipa-4',
    fakeDomain: 'secure-billing.co',
    type: 'Malicious Clone',
    dateDetected: '2026-04-05',
    severity: 'low',
    status: 'completed',
    recommendation: 'Report to Registrar | Enable DMARC',
    source: 'dark web API',
  },
  {
    id: 'ipa-5',
    fakeDomain: MOCK_DEMO_IPS.FAKE_DOMAIN_B,
    type: 'Botnet Log',
    dateDetected: '2026-04-14',
    severity: 'high',
    status: 'open',
    source: 'Alinet',
    recommendation: 'Block IP | Escalate',
  },
];

export const mockTacticDistribution: TacticDistribution[] = [
  { name: 'Phishing', value: 45, color: 'hsl(25 95% 53%)' },
  { name: 'Spoofed Email', value: 20, color: 'hsl(35 95% 55%)' },
  { name: 'Typosquatting', value: 25, color: 'hsl(0 72% 51%)' },
  { name: 'Social Media', value: 10, color: 'hsl(142 71% 45%)' },
];

export const mockMonitoredAssets: MonitoredAsset[] = [
  {
    id: 'ma-1',
    value: 'aminul.haque@ucb.com.bd',
    type: 'email',
    totalBreaches: 1,
    lastBreachDate: '2025-01-02',
    addedDate: '2024-06-01',
    isActive: true,
  },
  {
    id: 'ma-2',
    value: 'admin@corpsec.io',
    type: 'email',
    totalBreaches: 1,
    lastBreachDate: '2024-12-01',
    addedDate: '2024-05-15',
    isActive: true,
  },
  {
    id: 'ma-3',
    value: 'john.doe@example.com',
    type: 'email',
    totalBreaches: 1,
    lastBreachDate: '2024-10-18',
    addedDate: '2024-04-01',
    isActive: true,
  },
  {
    id: 'ma-4',
    value: 'security@bigcorp.net',
    type: 'email',
    totalBreaches: 1,
    lastBreachDate: '2025-01-10',
    addedDate: '2024-08-20',
    isActive: true,
  },
  {
    id: 'ma-5',
    value: MOCK_DEMO_IPS.INTERNAL_LAN,
    type: 'ip',
    totalBreaches: 1,
    lastBreachDate: '2025-01-25',
    addedDate: '2024-07-10',
    isActive: true,
  },
  {
    id: 'ma-6',
    value: MOCK_DEMO_IPS.PRIVATE_NETWORK,
    type: 'ip',
    totalBreaches: 1,
    lastBreachDate: '2024-12-15',
    addedDate: '2024-07-10',
    isActive: true,
  },
  {
    id: 'ma-7',
    value: MOCK_DEMO_IPS.DOCUMENTATION,
    type: 'ip',
    totalBreaches: 1,
    lastBreachDate: '2025-03-02',
    addedDate: '2024-09-01',
    isActive: true,
  },
  {
    id: 'ma-8',
    value: 'ucb.com.bd',
    type: 'domain',
    totalBreaches: 2,
    lastBreachDate: '2025-01-02',
    addedDate: '2024-03-01',
    isActive: true,
  },
  {
    id: 'ma-9',
    value: 'corpsec.io',
    type: 'domain',
    totalBreaches: 1,
    lastBreachDate: '2024-12-01',
    addedDate: '2024-03-01',
    isActive: true,
  },
];

export const mockBreachActivity: BreachActivityPoint[] = Array.from(
  { length: 90 },
  (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (89 - i));
    return {
      date: d.toISOString().slice(0, 10),
      critical: Math.floor(Math.random() * 3),
      high: Math.floor(Math.random() * 5),
      medium: Math.floor(Math.random() * 4),
      low: Math.floor(Math.random() * 2),
    };
  },
);
export const mockRiskTrend = Array.from({ length: 90 }, (_, i) => {
  const d = new Date();
  d.setDate(d.getDate() - (89 - i));
  const risk = Math.random();
  return {
    date: d.toISOString().slice(0, 10),
    riskLevel: risk > 0.7 ? 'high' : risk > 0.3 ? 'medium' : 'low',
    value: Math.floor(Math.random() * 8) + 1,
  };
});

export const mockRecommendations: Recommendation[] = [
  {
    id: 'r-1',
    title: 'Change Passwords',
    description:
      'Credentials for aminul.haque@ucb.com.bd were found in a dark web dump. Change the password immediately and revoke active sessions.',
    severity: 'critical',
    breachId: 'eb-1',
    actionType: 'change_password',
    isCompleted: false,
  },
  {
    id: 'r-2',
    title: 'Enable Two-Factor Authentication',
    description:
      'Enable 2FA for admin@corpsec.io to prevent unauthorized access after credential exposure.',
    severity: 'high',
    breachId: 'eb-2',
    actionType: 'enable_2fa',
    isCompleted: false,
  },
  {
    id: 'r-3',
    title: 'Block Exposed IP',
    description: mockIpBlockDescription(MOCK_DEMO_IPS.DOCUMENTATION),
    severity: 'critical',
    breachId: 'ib-3',
    actionType: 'block_ip',
    isCompleted: false,
  },
  {
    id: 'r-4',
    title: 'Rotate API Keys',
    description:
      'Credentials associated with cto@fintech.com may include API keys. Rotate all related keys.',
    severity: 'critical',
    breachId: 'eb-7',
    actionType: 'rotate_key',
    isCompleted: false,
  },
  {
    id: 'r-5',
    title: 'Notify Affected Users',
    description:
      'Inform users associated with bigcorp.net domain about the credential exposure.',
    severity: 'high',
    breachId: 'eb-4',
    actionType: 'notify_user',
    isCompleted: false,
  },
];

export const mockThreatIntelAlerts: ThreatIntelAlert[] = [
  {
    id: 'tia-1',
    threatDomain: 'login-secure-biz',
    description: 'Phishing Site',
    dateFound: '2026-04-11',
    severity: 'high',
    status: 'open',
    category: 'credential_leak',
    source: 'dark web API',
    recommendation: 'Analyze Threats | Enhance Monitoring',
  },
  {
    id: 'tia-2',
    threatDomain: 'company-support.net',
    description: 'Lookalike Domain',
    dateFound: '2026-04-19',
    severity: 'medium',
    status: 'open',
    category: 'malware',
    source: 'Alinet',
    recommendation: 'Report to Registrar | Block Domain',
  },
  {
    id: 'tia-3',
    threatDomain: 'corporate-alerts.com',
    description: 'Fake Email Campaign',
    dateFound: '2026-04-08',
    severity: 'high',
    status: 'in_mitigation',
    category: 'credential_leak',
    source: 'dark web forum',
    recommendation: 'Analyze Threats | Notify Affected Users',
  },
  {
    id: 'tia-4',
    threatDomain: 'secure-billing.co',
    description: 'Malicious Clone',
    dateFound: '2026-04-05',
    severity: 'low',
    status: 'completed',
    category: 'data_leak',
    source: 'dark web API',
    recommendation: 'Continue Monitoring',
  },
  {
    id: 'tia-5',
    threatDomain: 'banking-update.xyz',
    description: 'Credential Harvesting',
    dateFound: '2026-04-13',
    severity: 'critical',
    status: 'open',
    category: 'credential_leak',
    source: 'Alinet',
    recommendation: 'Analyze Threats | Escalate Immediately',
  },
  {
    id: 'tia-6',
    threatDomain: 'support-desk-portal.net',
    description: 'Ransomware Distribution',
    dateFound: '2026-04-10',
    severity: 'high',
    status: 'in_mitigation',
    category: 'ransomware',
    source: 'dark web forum',
    recommendation: 'Block Domain | Isolate Affected Systems',
  },
];

export const mockAlertTrend = Array.from({ length: 90 }, (_, i) => {
  const d = new Date();
  d.setDate(d.getDate() - (89 - i));
  return {
    date: d.toISOString().slice(0, 10),
    high: Math.floor(Math.random() * 80) + 40,
    medium: Math.floor(Math.random() * 60) + 20,
    low: Math.floor(Math.random() * 30) + 10,
  };
});

export const mockThreatTypesData: ThreatTypeData[] = [
  { name: 'Credentials', value: 145, color: 'hsl(0 72% 51%)' },
  { name: 'Ransomware', value: 85, color: 'hsl(25 95% 53%)' },
  { name: 'Data Leaks', value: 62, color: 'hsl(45 93% 47%)' },
  { name: 'Malware', value: 38, color: 'hsl(210 100% 50%)' },
];

export const mockVulnerabilityAlerts: VulnerabilityAlert[] = [
  {
    id: 'va-1',
    cveId: 'CVE-2026-1234',
    vulnerability: 'Remote Code Execution',
    riskLevel: 'critical',
    status: 'unpatched',
    description:
      'A critical remote code execution vulnerability allows attackers to execute arbitrary code on the target system without authentication.',
    affectedSystem: 'Web Application Server',
    dateDetected: '2026-04-11',
    recommendation: 'Apply Updates | Conduct Scans',
  },
  {
    id: 'va-2',
    cveId: 'CVE-2026-0987',
    vulnerability: 'SQL Injection Vulnerability',
    riskLevel: 'high',
    status: 'in_progress',
    description:
      'SQL injection vulnerability in the login form allows attackers to bypass authentication and access sensitive data.',
    affectedSystem: 'Database Server',
    dateDetected: '2026-04-09',
    recommendation: 'Apply Parameterized Queries | Update ORM',
  },
  {
    id: 'va-3',
    cveId: 'CVE-2026-0456',
    vulnerability: 'Outdated Software Exploit',
    riskLevel: 'medium',
    status: 'open',
    description:
      'Outdated library version contains known vulnerabilities that can be exploited for privilege escalation.',
    affectedSystem: 'Application Dependencies',
    dateDetected: '2026-04-08',
    recommendation: 'Update Dependencies | Review Changelog',
  },
  {
    id: 'va-4',
    cveId: 'CVE-2026-0456',
    vulnerability: 'Ruleitcen Close',
    riskLevel: 'low',
    status: 'patched',
    description:
      'Minor configuration issue that has been resolved through a patch update.',
    affectedSystem: 'Firewall Configuration',
    dateDetected: '2026-04-05',
    recommendation: 'Continue Monitoring',
  },
  {
    id: 'va-5',
    cveId: 'CVE-2026-2001',
    vulnerability: 'Cross-Site Scripting (XSS)',
    riskLevel: 'high',
    status: 'open',
    description:
      'Stored XSS vulnerability in the comment section allows injection of malicious scripts.',
    affectedSystem: 'Frontend Application',
    dateDetected: '2026-04-12',
    recommendation: 'Sanitize Input | Apply CSP Headers',
  },
  {
    id: 'va-6',
    cveId: 'CVE-2026-3050',
    vulnerability: 'Privilege Escalation',
    riskLevel: 'critical',
    status: 'unpatched',
    description:
      'Local privilege escalation allows standard users to gain administrator access.',
    affectedSystem: 'Operating System',
    dateDetected: '2026-04-13',
    recommendation: 'Apply Patch | Restrict User Permissions',
  },
];

export const mockRemediationTrend = Array.from({ length: 90 }, (_, i) => {
  const d = new Date();
  d.setDate(d.getDate() - (89 - i));
  return {
    date: d.toISOString().slice(0, 10),
    remediated: Math.floor(10 + i * 3.5 + Math.random() * 15),
  };
});

export const mockRiskDistribution: RiskDistData[] = [
  { name: 'Critical', value: 25, color: 'hsl(25 95% 53%)' },
  { name: 'Researcher', value: 25, color: 'hsl(0 72% 51%)' },
  { name: 'Fish Tove', value: 40, color: 'hsl(45 93% 47%)' },
  { name: 'Meaute', value: 20, color: 'hsl(210 100% 50%)' },
];

export const mockImpersonationAlerts: ImpersonationAlert[] = [
  {
    id: 'imp-1',
    fakeDomain: MOCK_DEMO_IPS.FAKE_DOMAIN_A,
    type: 'Phishing Site',
    dateDetected: '2026-04-12',
    severity: 'high',
    status: 'open',
    source: 'dark web API',
    recommendation: 'Report to Registrar | Enable DMARC',
    description:
      'A phishing website mimicking the company login page was detected targeting employee credentials.',
  },
  {
    id: 'imp-2',
    fakeDomain: MOCK_DEMO_IPS.FAKE_DOMAIN_B,
    type: 'Botnet Log',
    dateDetected: '2026-04-14',
    severity: 'high',
    status: 'open',
    source: 'Alinet',
    recommendation: 'Block IP | Escalate',
    description:
      'IP address found in botnet command-and-control logs, potentially used for impersonation campaigns.',
  },
  {
    id: 'imp-3',
    fakeDomain: MOCK_DEMO_IPS.FAKE_DOMAIN_C,
    type: 'Exploit Database',
    dateDetected: '2026-04-08',
    severity: 'medium',
    status: 'open',
    source: 'dark web forum',
    recommendation: 'Apply Patch | Monitor',
    description:
      'Exploit database entry referencing company infrastructure for potential impersonation attacks.',
  },
  {
    id: 'imp-4',
    fakeDomain: 'secure-billing.co',
    type: 'Malicious Clone',
    dateDetected: '2026-04-05',
    severity: 'low',
    status: 'resolved',
    source: 'dark web API',
    recommendation: 'Report to Registrar | Enable DMARC',
    description:
      'A cloned version of the company billing portal was identified and has been taken down.',
  },
  {
    id: 'imp-5',
    fakeDomain: MOCK_DEMO_IPS.FAKE_DOMAIN_C,
    type: 'Exploit Database',
    dateDetected: '2026-04-08',
    severity: 'medium',
    status: 'open',
    source: 'dark web forum',
    recommendation: 'Apply Patch | Monitor',
    description:
      'Exploit database entry referencing company infrastructure for potential impersonation attacks.',
  },
];
