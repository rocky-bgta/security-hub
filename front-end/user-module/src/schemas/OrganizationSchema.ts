import { array, object, string, z } from 'zod';

export const AddOrganizationFormSchema = object({
  organizationName: string()
    .min(1, 'Organization name is required')
    .regex(/^([a-zA-Z\s*.]{2,})$/, {
      message:
        'Name must contain at least two characters and only letters, spaces, and dots.',
    }),
  role: string().min(1, 'Please select a role'),
  organizationType: string().min(1, 'Please select a organization type'),
  email: string()
    .email('Invalid email address')
    .refine(email => isValidCompanyEmail(email), {
      message: 'Must be valid company email',
    }),

  domain: string().min(1, 'Invalid domain'),
  phone: string().min(1, 'Phone domain'),
  DefaultLanguage: string().min(1, 'Please select a language'),
  country: string().min(1, 'Please select a country'),
  timeZone: string().min(1, 'Please select a time zone'),
  organizationSize: string().min(1, 'Please select a organization size'),
  Industry: string().min(1, 'Please select a Industry'),
  techName: string()
    .min(1, 'Tech Name is required')
    .regex(/^([a-zA-Z\s*.]{2,})$/, {
      message:
        'Tech Name must contain at least two characters and only letters, spaces, and dots.',
    }),
  techEmail: string()
    .email('Invalid Tech email address')
    .refine(techEmail => isValidCompanyEmail(techEmail), {
      message: 'Must be valid company email',
    })
    .optional(),
  billingName: string()
    .min(1, 'Billing Name is required')
    .regex(/^([a-zA-Z\s*.]{2,})$/, {
      message:
        'Billing name must contain at least two characters and only letters, spaces, and dots.',
    }),
  billingEmail: string()
    .email('Invalid Billing email address')
    .refine(billingEmail => isValidCompanyEmail(billingEmail), {
      message: 'Must be valid company email',
    })
    .optional(),
  companyAddress: string().min(1, 'Please select a organization type'),
});

export type TAddOrganizationFormFields = z.infer<
  typeof AddOrganizationFormSchema
>;
export type TAddOrganizationFormFieldKeys = keyof TAddOrganizationFormFields;

export const DefaultAddOrganizationFormValues = {
  organizationName: '',
  role: '',
  organizationType: '',
  email: '',
  domain: '',
  phone: '',
  DefaultLanguage: '',
  country: '',
  timeZone: '',
  organizationSize: '',
  Industry: '',
  techName: '',
  techEmail: '',
  billingName: '',
  billingEmail: '',
  companyAddress: '',
};

export const AddOrganizationFormFields = [
  {
    name: 'Organization Name',
    key: 'organizationName',
    placeholder: 'Organization Name',
  },
  {
    name: 'Role',
    key: 'role',
    placeholder: 'Select Role',
  },
  {
    name: 'Organization Type',
    key: 'organizationType',
    placeholder: 'Select Organization Type',
  },
  {
    name: 'Organization Email',
    key: 'email',
    placeholder: 'Email',
  },
  {
    name: 'Domain',
    key: 'domain',
    placeholder: 'Domain',
  },
  {
    name: 'Phone',
    key: 'phone',
    placeholder: 'Phone',
  },
  {
    name: 'Default Language',
    key: 'DefaultLanguage',
    placeholder: 'Select Language',
  },
  { name: 'Country', key: 'country', placeholder: 'Select Country' },
  { name: 'Time Zone', key: 'timeZone', placeholder: 'Select Time Zone' },
  {
    name: 'Organization Size',
    key: 'organizationSize',
    placeholder: 'Select organization Size',
  },
  {
    name: 'Industry',
    key: 'Industry',
    placeholder: 'Select Industry',
  },
  {
    name: 'Tech Name',
    key: 'techName',
    placeholder: 'Tech Name',
  },
  {
    name: 'Tech Email',
    key: 'techEmail',
    placeholder: 'Tech email',
  },
  {
    name: 'Billing Name',
    key: 'billingName',
    placeholder: 'Billing Name',
  },
  {
    name: 'Billing Email',
    key: 'billingEmail',
    placeholder: 'Billing email',
  },
  {
    name: 'Company Address',
    key: 'companyAddress',
    placeholder: 'Company Address',
  },
];

export const InviteOrganizationSchema = object({
  organizations: array(
    object({
      name: string().min(1, 'Name is required'),
      email: z
        .string()
        .email('Invalid email address')
        .refine(email => email !== '', 'Email is required'),
      role: string().min(1, 'Role is required'),
      phone: string().optional(),
    }),
  ).nonempty(),
});

const isValidCompanyEmail = (email: string) => {
  const [_, domainPart] = email.split('@');
  if (!domainPart) return true; // Skip refinement if there's no domain part
  const [domain, __] = domainPart.split('.');
  return ![
    'gmail',
    'yahoo',
    'hotmail',
    'outlook',
    'aol',
    'proton',
    'protonmail',
    'zohomail',
    'gmx',
    'gmxmail',
    'mail2world',
    'icloud',
    'tuta',
    '10minutemail',
    'juno',
    'mailtrap',
  ].includes(domain);
};
