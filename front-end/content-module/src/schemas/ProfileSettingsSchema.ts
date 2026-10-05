import { object, string, z } from 'zod';

const PERSON_NAME_REGEX = /^[A-Za-z .'-]+$/;

const createPersonNameSchema = (label: 'First name' | 'Last name') =>
  string()
    .min(1, `${label} is required.`)
    .min(3, `${label} must be at least 3 characters long.`)
    .max(50, `${label} cannot exceed 50 characters.`)
    .regex(
      PERSON_NAME_REGEX,
      `${label} can contain letters, spaces, hyphens, apostrophes, and dots only.`,
    );

const phoneSchema = string()
  .min(1, 'Phone number is required.')
  .superRefine((value, ctx) => {
    if (/[a-zA-Z]/.test(value)) {
      ctx.addIssue({
        code: 'custom',
        message: 'Phone number can contain numbers only.',
      });
      return;
    }

    const digits = value.replace(/\D/g, '');

    if (value.length > 0 && digits.length === 0) {
      ctx.addIssue({
        code: 'custom',
        message: 'Phone number can contain numbers only.',
      });
      return;
    }

    if (digits.length < 7) {
      ctx.addIssue({
        code: 'custom',
        message: 'Phone number must be at least 7 digits long.',
      });
      return;
    }
    if (digits.length > 15) {
      ctx.addIssue({
        code: 'custom',
        message: 'Phone number cannot exceed 15 digits.',
      });
    }
  });

export const normalizePhoneValue = (phone?: string) =>
  (phone || '').replace(/\D/g, '');

export const ProfileSettingsSchema = object({
  FirstName: createPersonNameSchema('First name'),
  LastName: createPersonNameSchema('Last name'),
  Email: string()
    .trim()
    .refine(email => {
      if (!email) return true;
      const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      return emailRegex.test(email);
    }, 'Please enter a valid email address'),
  Phone: phoneSchema,
  Department: string(),
});

export type TProfileSettingsFormFields = z.infer<typeof ProfileSettingsSchema>;
export type TProfileSettingsFormFieldsKeys = keyof TProfileSettingsFormFields;

export const DefaultProfileSettingsValues = {
  FirstName: '',
  LastName: '',
  Email: '',
  Phone: '',
  OrganizationName: '',
  Department: '',
  Designation: '',
  Country: '',
  Address: '',
  TimeZone: '',
  SupervisorName: '',
  SupervisorEmail: '',
  Language: '',
};

export const ProfileSettingsFormFields = [
  {
    section: 'Profile Information',
    fields: [
      {
        key: 'FirstName',
        name: 'First Name',
        type: 'text',
        placeholder: 'Enter your first name',
        required: true,
      },
      {
        key: 'LastName',
        name: 'Last Name',
        type: 'text',
        placeholder: 'Enter your last name',
        required: true,
      },
      {
        key: 'Email',
        name: 'Email',
        type: 'email',
        placeholder: 'Enter your email',
        required: false,
      },
      {
        key: 'Phone',
        name: 'Phone',
        type: 'tel',
        placeholder: 'Enter your phone number',
        required: true,
      },
      {
        key: 'Department',
        name: 'Department',
        type: 'select',
        placeholder: 'Select your department',
        required: false,
      },
    ],
  },
];
