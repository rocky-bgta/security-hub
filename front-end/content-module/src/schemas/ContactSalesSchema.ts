import { object, string, z } from 'zod';

const PERSON_NAME_REGEX = /^[A-Za-z .'-]+$/;

const createPersonNameSchema = (label: 'First name' | 'Last name') =>
  string()
    .min(1, `${label} is required.`)
    .min(2, `${label} must be at least 2 characters long.`)
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

export const ContactSalesSchema = object({
  email: string()
    .min(1, 'Email address is required.')
    .email('Please enter a valid email address.'),
  phone: phoneSchema,
  firstName: createPersonNameSchema('First name'),
  lastName: createPersonNameSchema('Last name'),
  companyName: string()
    .min(1, 'Company name is required.')
    .min(2, 'Company name must be at least 2 characters long.')
    .max(100, 'Company name cannot exceed 100 characters.'),
  numberOfEmployees: string().min(1, 'Number of employees is required.'),
  hearAboutUs: string().min(1, 'Please tell us how you heard about us.'),
});

export type TContactSalesFormFields = z.infer<typeof ContactSalesSchema>;

export const DefaultContactSalesValues: TContactSalesFormFields = {
  email: '',
  phone: '',
  firstName: '',
  lastName: '',
  companyName: '',
  numberOfEmployees: '',
  hearAboutUs: '',
};

export const HEAR_ABOUT_US_OPTIONS = [
  'Search Engine',
  'Social Media',
  'Referral',
  'Email Campaign',
  'Event / Webinar',
  'Advertisement',
  'Other',
];
