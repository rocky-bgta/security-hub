export type NotificationType = string;

export interface INotificationSettings {
  id: string;
  notificationType: NotificationType;
  enabled: boolean;
  description: string;
  emailEnabled: true;
  inAppEnabled: boolean;
  smsEnabled: boolean;
  pushEnabled: boolean;
  defaultEmailTemplateId: string | null;
  defaultInAppTemplateId: string | null;
  defaultSmsTemplateId: string | null;
  defaultPushTemplateId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface IClientAdminNotificationSettings {
  id: string | null;
  clientAdminId: string;
  notificationType: NotificationType;
  enabled: boolean;
  emailEnabled: boolean;
  inAppEnabled: boolean;
  smsEnabled: boolean;
  pushEnabled: boolean;
  createdAt: string;
  updatedAt: string;
  customized: boolean;
}

export interface IRoleNotificationSettings {
  id: string;
  notificationType: NotificationType;
  role: string;
  enabled: boolean;
  emailEnabled: boolean;
  inAppEnabled: boolean;
  smsEnabled: boolean;
  pushEnabled: boolean;
  phoneCallEnabled: boolean;
  customized: boolean;
  createdAt: string;
  updatedAt: string;
}

export const NOTIFICATION_ROLES = [
  { value: 'ASPIRE_ADMIN', label: 'Aspire Admin' },
  { value: 'CLIENT_ADMIN', label: 'Client Admin' },
  { value: 'MSP', label: 'MSP' },
  { value: 'USER', label: 'User' },
] as const;

export enum NotificationChannel {
  EMAIL = 'EMAIL',
  IN_APP = 'IN_APP',
  SMS = 'SMS',
}

export const NOTIFICATION_CHANNELS = [
  { value: NotificationChannel.EMAIL, label: 'Email' },
  { value: NotificationChannel.IN_APP, label: 'In-App' },
  { value: NotificationChannel.SMS, label: 'SMS' },
] as const;

export interface INotificationTemplate {
  id: string;
  notificationType: NotificationType;
  channel: NotificationChannel;
  recipientRole: string;
  templateName: string;
  subjectTemplate: string;
  htmlTemplate: string;
  textTemplate: string;
  titleTemplate: string;
  messageTemplate: string;
  organizationId: string;
  createdAt: string;
  updatedAt: string;
  active: boolean;
  default: boolean;
}

export enum NotificationTemplateAction {
  UPDATE = 'UPDATE',
}

export interface IUpdateNotificationTemplatePayload {
  action: NotificationTemplateAction.UPDATE;
  templateId: string;
  notificationType: string;
  channel: string;
  templateName: string;
  recipientRole: string;
  subjectTemplate: string;
  htmlTemplate: string;
  textTemplate: string;
  titleTemplate: string;
  messageTemplate: string;
  isActive: boolean;
  isDefault: boolean;
  organizationId: string;
}

export const toUpdateNotificationTemplatePayload = (
  template: INotificationTemplate,
  overrides: Partial<
    Pick<
      IUpdateNotificationTemplatePayload,
      | 'templateName'
      | 'subjectTemplate'
      | 'htmlTemplate'
      | 'textTemplate'
      | 'messageTemplate'
      | 'isActive'
      | 'isDefault'
    >
  >,
): IUpdateNotificationTemplatePayload => ({
  action: NotificationTemplateAction.UPDATE,
  templateId: template.id,
  notificationType: template.notificationType,
  channel: template.channel,
  templateName: overrides.templateName ?? template.templateName,
  recipientRole: template.recipientRole,
  subjectTemplate: overrides.subjectTemplate ?? template.subjectTemplate,
  htmlTemplate: overrides.htmlTemplate ?? template.htmlTemplate,
  textTemplate: overrides.textTemplate ?? template.textTemplate,
  titleTemplate: template.titleTemplate,
  messageTemplate: overrides.messageTemplate ?? template.messageTemplate,
  isActive: overrides.isActive ?? template.active,
  isDefault: overrides.isDefault ?? template.default,
  organizationId: template.organizationId,
});

export enum NotificationAction {
  ENABLE = 'ENABLE',
  DISABLE = 'DISABLE',
  UPDATE = 'UPDATE_CHANNELS',
}

export interface IGlobalChannels {
  email: boolean;
  inApp: boolean;
  sms: boolean;
  push: boolean;
}

export const NOTIFICATION_CATEGORIES = {
  'User Management': [
    'NEW_USER_REGISTERED',
    'WELCOME_EMAIL',
    'USER_PROFILE_UPDATED',
    'PASSWORD_RESET_REQUEST',
    'USER_SUSPENDED',
    'USER_PASSWORD_CHANGE',
    'USER_PASSWORD_CHANGE_ADMIN',
    'BULK_USER_IMPORT_SUMMARY',
  ],
  'Training & Courses': [
    'NEW_COURSE_CREATED',
    'COURSE_UPDATED',
    'COURSE_ASSIGNED',
    'COURSE_COMPLETION',
    'NEW_PACKAGE_CREATED',
    'PACKAGE_ASSIGNED',
    'PACKAGE_ASSIGNED_USER',
    'PACKAGE_EXPIRY',
  ],
  'Payments & Billing': [
    'PAYMENT_SUCCESS',
    'PAYMENT_FAILURE',
    'PENDING_PAYMENT',
    'REFUND_REQUESTED',
    'SUBSCRIPTION_RENEWAL_REMINDER',
  ],
  'Compliance & Policies': [
    'NEW_POLICY_CREATED',
    'POLICY_UPDATED',
    'POLICY_COMPLIANCE_REMINDER',
  ],
  Certificates: [
    'CERTIFICATE_ISSUED',
    'CERTIFICATE_ISSUED_ADMIN',
    'CERTIFICATE_EXPIRY',
    'CERTIFICATE_REVOKED',
  ],
  'System & Security': [
    'SYSTEM_HEALTH_ALERTS',
    'SECURITY_ALERTS',
    'UPDATES_PATCHES',
    'FEATURE_UPDATE_CHANGE',
    'SCHEDULED_MAINTENANCE',
  ],
  Other: [
    'CAMPAIGN_ACTIVITY',
    'CLIENT_ADMIN_PACKAGE_ASSIGNED',
    'LEADERBOARD_UPDATE',
    'REMINDER_PENDING_TASKS',
    'HELP_DESK_TICKET_UPDATES',
  ],
};
