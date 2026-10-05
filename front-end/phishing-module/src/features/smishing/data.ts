import {
  Calendar,
  Globe,
  GraduationCap,
  MessageSquare,
  Rocket,
  ServerCog,
  ShieldCheck,
  Tag as TagIcon,
  Users,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';

export const OWNERS = [
  'Alex Carter',
  'Priya Singh',
  'Marcus Vega',
  'Linh Tran',
  'Sara Müller',
];

export const SMS_TEMPLATES = [
  {
    id: 'tpl-bank',
    name: 'Bank Account Alert',
    body: 'Hi {first_name}, suspicious activity detected on your {company_name} account. Verify now: {url}',
  },
  {
    id: 'tpl-mfa',
    name: 'MFA Verification',
    body: "{first_name}, your {company_name} verification code is 482910. If you didn't request, tap {url}",
  },
  {
    id: 'tpl-hr',
    name: 'HR Policy Update',
    body: 'Hello {first_name}, please acknowledge the new {company_name} HR policy here: {url}',
  },
  {
    id: 'tpl-pkg',
    name: 'Package Delivery',
    body: 'Hi {first_name}, your package is on hold. Reschedule delivery: {url}',
  },
];

export const PLACEHOLDERS = [
  '{first_name}',
  '{last_name}',
  '{company_name}',
  '{department}',
  '{url}',
];

export const LANDING_PAGES = [
  {
    id: 'lp-m365',
    name: 'Microsoft 365 Login',
    category: 'Login Portal',
    risk: 'High',
  },
  {
    id: 'lp-google',
    name: 'Google Workspace Login',
    category: 'Login Portal',
    risk: 'High',
  },
  {
    id: 'lp-bank',
    name: 'Online Banking Portal',
    category: 'Finance',
    risk: 'Critical',
  },
  {
    id: 'lp-hr',
    name: 'HR Self-Service Portal',
    category: 'Internal',
    risk: 'Medium',
  },
  {
    id: 'lp-pkg',
    name: 'Package Tracking',
    category: 'Logistics',
    risk: 'Low',
  },
];

export type GatewayProvider = {
  id: string;
  name: string;
  region: string;
  country: string;
  endpoint: string;
  connected: boolean;
  previouslyUsed?: boolean;
  tag?: 'Recommended' | 'Popular' | 'High Delivery Rate';
};

export const GATEWAYS: GatewayProvider[] = [
  {
    id: 'twilio',
    name: 'Twilio SMS',
    region: 'Global',
    country: 'GLOBAL',
    endpoint: 'api.twilio.com',
    connected: true,
    tag: 'Recommended',
    previouslyUsed: true,
  },
  {
    id: 'vonage',
    name: 'Vonage (Nexmo)',
    region: 'Global',
    country: 'GLOBAL',
    endpoint: 'rest.nexmo.com',
    connected: true,
    tag: 'Popular',
  },
  {
    id: 'infobip',
    name: 'Infobip',
    region: 'EU / APAC',
    country: 'GLOBAL',
    endpoint: 'api.infobip.com',
    connected: false,
  },
  {
    id: 'msg91',
    name: 'MSG91',
    region: 'India',
    country: 'IN',
    endpoint: 'api.msg91.com',
    connected: true,
    tag: 'High Delivery Rate',
  },
  {
    id: 'bulksmsbd',
    name: 'BulkSMS BD',
    region: 'Bangladesh',
    country: 'BD',
    endpoint: 'api.bulksmsbd.net',
    connected: true,
    tag: 'Recommended',
  },
  {
    id: 'robi',
    name: 'Robi SMS',
    region: 'Bangladesh',
    country: 'BD',
    endpoint: 'api.robi.com.bd',
    connected: false,
    tag: 'Popular',
  },
  {
    id: 'banglalink',
    name: 'Banglalink SMS',
    region: 'Bangladesh',
    country: 'BD',
    endpoint: 'sms.banglalink.net',
    connected: false,
  },
  {
    id: 'bandwidth',
    name: 'Bandwidth',
    region: 'USA',
    country: 'US',
    endpoint: 'messaging.bandwidth.com',
    connected: false,
    tag: 'High Delivery Rate',
  },
  {
    id: 'plivo',
    name: 'Plivo',
    region: 'USA',
    country: 'US',
    endpoint: 'api.plivo.com',
    connected: true,
    tag: 'Popular',
  },
];

export const USER_COUNTRY = 'BD';

export function calcSegments(text: string) {
  const len = text.length;
  if (len === 0) return { chars: 0, segments: 0, perSegment: 160 };
  if (len <= 160) return { chars: len, segments: 1, perSegment: 160 };
  return { chars: len, segments: Math.ceil(len / 153), perSegment: 153 };
}

export const PACKAGES = [
  'Bronze Package',
  'Silver Package',
  'Gold Package',
  'Platinum Package',
];

export const TRAINING_FILTERS = {
  country: ['Global', 'US', 'EU', 'UK', 'India'],
  compliance: ['GDPR', 'HIPAA', 'PCI-DSS', 'SOX', 'ISO 27001'],
  category: [
    'Phishing',
    'Social Engineering',
    'Cloud Security',
    'Data Privacy',
    'Insider Threat',
  ],
  difficulty: ['Beginner', 'Intermediate', 'Advanced'],
  tone: ['Formal', 'Conversational', 'Story-based'],
};

export const TRAINING_MODULES = [
  {
    id: 'tm-ceo',
    title: 'CEO Fraud Defense',
    desc: 'Detect impersonation attempts targeting executives and finance.',
    tags: ['Social Engineering', 'Role-Based Topics'],
    type: 'Video',
    duration: '8 min',
  },
  {
    id: 'tm-cloud',
    title: 'Cloud Security Threats',
    desc: 'Cover SaaS exposure, OAuth abuse, and unsafe sharing patterns.',
    tags: ['Cloud Security', 'Data Protection'],
    type: 'Article',
    duration: '12 min',
  },
  {
    id: 'tm-smish',
    title: 'Spotting Smishing Attempts',
    desc: 'Identify telltale signs of malicious SMS and short links.',
    tags: ['Phishing', 'Security Awareness'],
    type: 'Video',
    duration: '6 min',
  },
  {
    id: 'tm-mfa',
    title: 'MFA Bypass Awareness',
    desc: 'Understand prompt-bombing, SIM swap, and adversary-in-the-middle.',
    tags: ['Phishing', 'Identity'],
    type: 'Video',
    duration: '10 min',
  },
  {
    id: 'tm-vish',
    title: 'Vishing Red Flags',
    desc: 'Spot urgency, authority, and pretexting cues on phone calls.',
    tags: ['Social Engineering'],
    type: 'Article',
    duration: '9 min',
  },
  {
    id: 'tm-data',
    title: 'Data Handling Essentials',
    desc: 'Classify, share, and dispose of sensitive data correctly.',
    tags: ['Data Protection', 'Security Awareness'],
    type: 'Video',
    duration: '11 min',
  },
  {
    id: 'tm-pwd',
    title: 'Password Hygiene 101',
    desc: 'Build strong, unique passwords and adopt a password manager.',
    tags: ['Security Awareness'],
    type: 'Article',
    duration: '5 min',
  },
  {
    id: 'tm-rep',
    title: 'Reporting Suspicious SMS',
    desc: 'Use the report button and escalate to security swiftly.',
    tags: ['Role-Based Topics'],
    type: 'Video',
    duration: '4 min',
  },
];

export const SEED_USERS = [
  {
    id: 'u1',
    name: 'Gray Sweeney',
    email: 'user4@onystyle.com',
    department: 'Legal & Compliance',
    status: 'Active',
    risk: 'Low Risk',
  },
  {
    id: 'u2',
    name: 'Bree Mcclure',
    email: 'user3@onystyle.com',
    department: 'Legal & Compliance',
    status: 'Active',
    risk: 'Low Risk',
  },
  {
    id: 'u3',
    name: 'Daphne Wolf',
    email: 'user2@onystyle.com',
    department: 'Legal & Compliance',
    status: 'Active',
    risk: 'Low Risk',
  },
  {
    id: 'u4',
    name: 'Violet Madden',
    email: 'user1@onystyle.com',
    department: 'Product Development',
    status: 'Active',
    risk: 'Medium Risk',
  },
  {
    id: 'u5',
    name: 'Marcus Vega',
    email: 'marcus@onystyle.com',
    department: 'Finance',
    status: 'Active',
    risk: 'High Risk',
  },
  {
    id: 'u6',
    name: 'Linh Tran',
    email: 'linh@onystyle.com',
    department: 'Engineering',
    status: 'Inactive',
    risk: 'Medium Risk',
  },
  {
    id: 'u7',
    name: 'Sara Müller',
    email: 'sara@onystyle.com',
    department: 'HR',
    status: 'Active',
    risk: 'Low Risk',
  },
  {
    id: 'u8',
    name: 'Sara Bartlett',
    email: 'loqexah@yopmail.com',
    department: 'Customer Service/Support',
    status: 'Active',
    risk: 'High Risk',
  },
] as const;

export const ALL_DEPARTMENTS = Array.from(
  new Set(SEED_USERS.map(u => u.department)),
);
export const ALL_STATUSES = ['Active', 'Inactive'];
export const ALL_RISKS = ['Low Risk', 'Medium Risk', 'High Risk'];
export const AUDIENCE_MODES = [
  { id: 'all', label: 'All Users' },
  { id: 'dept', label: 'By Department' },
  { id: 'risk', label: 'By Risk Group' },
  { id: 'individual', label: 'Individual Selection' },
] as const;
export type AudienceMode = (typeof AUDIENCE_MODES)[number]['id'];

export const TIMEZONES = [
  'UTC',
  'America/New_York',
  'Europe/London',
  'Europe/Berlin',
  'Asia/Kolkata',
  'Asia/Singapore',
];

export type StepKey =
  | 'setup'
  | 'template'
  | 'landing'
  | 'gateway'
  | 'tags'
  | 'audience'
  | 'training'
  | 'schedule'
  | 'review';

export type StepDef = {
  key: StepKey;
  title: string;
  subtitle: string;
  icon: LucideIcon;
};

export const BASE_STEPS: StepDef[] = [
  {
    key: 'setup',
    title: 'Setup',
    subtitle: 'Initialize campaign',
    icon: ShieldCheck,
  },
  {
    key: 'template',
    title: 'SMS Template',
    subtitle: 'Craft message',
    icon: MessageSquare,
  },
  {
    key: 'landing',
    title: 'Landing Page',
    subtitle: 'Tracking setup',
    icon: Globe,
  },
  {
    key: 'gateway',
    title: 'SMS Gateway',
    subtitle: 'Provider & sender',
    icon: ServerCog,
  },
  {
    key: 'tags',
    title: 'Tags & Metadata',
    subtitle: 'Organize',
    icon: TagIcon,
  },
  {
    key: 'audience',
    title: 'Audience',
    subtitle: 'Select recipients',
    icon: Users,
  },
  {
    key: 'schedule',
    title: 'Schedule',
    subtitle: 'Timing & pattern',
    icon: Calendar,
  },
  {
    key: 'review',
    title: 'Review & Launch',
    subtitle: 'Test SMS & go live',
    icon: Rocket,
  },
];

export const TRAINING_STEP: StepDef = {
  key: 'training',
  title: 'Training & Content',
  subtitle: 'Bundle modules',
  icon: GraduationCap,
};
