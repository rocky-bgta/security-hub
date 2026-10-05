import { array, object, string, z } from 'zod';
import { SupportTicketPriority } from 'models/SupportTicket';
import {
  validateSupportTicketDescription,
  validateSupportTicketSubject,
} from 'utils/SupportTicketValidation';

export { validateSupportTicketComment } from 'utils/SupportTicketValidation';

export const supportTicketSubjectField = string().superRefine((val, ctx) => {
  const error = validateSupportTicketSubject(val);
  if (error) {
    ctx.addIssue({ code: 'custom', message: error });
  }
});

export const supportTicketDescriptionField = string().superRefine((val, ctx) => {
  const error = validateSupportTicketDescription(val);
  if (error) {
    ctx.addIssue({ code: 'custom', message: error });
  }
});

// User Support Ticket Schema
export const userSupportTicketSchema = object({
  title: supportTicketSubjectField,
  supportType: string().min(1, 'Support type is required'),
  course: string().min(1, 'Course is required'),
  priority: z.nativeEnum(SupportTicketPriority, {
    message: 'Priority is required',
  }),
  description: supportTicketDescriptionField,
  attachments: array(string()).optional(),
});

export type UserSupportTicketFormData = z.infer<typeof userSupportTicketSchema>;
export type UserSupportTicketFormDataKeys = keyof UserSupportTicketFormData;

export const DefaultUserSupportTicketFormValues: UserSupportTicketFormData = {
  title: '',
  supportType: '',
  course: '',
  priority: '' as SupportTicketPriority,
  description: '',
  attachments: [],
};

export const UserSupportTicketFormFieldsKeys: UserSupportTicketFormDataKeys[] =
  ['supportType', 'course', 'priority', 'title', 'description', 'attachments'];

// Client Admin Support Ticket Schema
export const clientAdminSupportTicketSchema = object({
  title: supportTicketSubjectField,
  supportType: string().min(1, 'Support type is required'),
  product: string().min(1, 'Product is required'),
  package: string().min(1, 'Package is required'),
  priority: z.nativeEnum(SupportTicketPriority, {
    message: 'Priority is required',
  }),
  description: supportTicketDescriptionField,
  attachments: array(string()).optional(),
});

export type ClientAdminSupportTicketFormData = z.infer<
  typeof clientAdminSupportTicketSchema
>;
export type ClientAdminSupportTicketFormDataKeys =
  keyof ClientAdminSupportTicketFormData;

export const DefaultClientAdminSupportTicketFormValues: ClientAdminSupportTicketFormData =
  {
    title: '',
    supportType: '',
    product: '',
    package: '',
    priority: '' as SupportTicketPriority,
    description: '',
    attachments: [],
  };

export const ClientAdminSupportTicketFormFieldsKeys: ClientAdminSupportTicketFormDataKeys[] =
  [
    'supportType',
    'product',
    'package',
    'priority',
    'title',
    'description',
    'attachments',
  ];
