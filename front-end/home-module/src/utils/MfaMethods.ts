import { LucideIcon, Mail, Phone, QrCode, Smartphone } from 'lucide-react';

export interface IMfaMethodUi {
  method: string;
  icon: LucideIcon;
  header: string;
  description: string;
}

export const MFA_METHODS: Array<IMfaMethodUi> = [
  {
    method: 'AUTHENTICATOR',
    icon: QrCode,
    header: 'Authenticator App',
    description: 'Use Google Authenticator or similar apps.',
  },
  {
    method: 'SMS',
    icon: Smartphone,
    header: 'SMS Verification',
    description: 'Receive a code via text sms.',
  },
  {
    method: 'EMAIL',
    icon: Mail,
    header: 'Email OTP',
    description: 'Receive a code via email.',
  },
  {
    method: 'PHONE_CALL',
    icon: Phone,
    header: 'Phone Call',
    description: 'Receive a code via phone call.',
  },
];

export const getMfaMethodUi = (method: string) =>
  MFA_METHODS.find(item => item.method === method);
