import { Status } from 'models/Global';
import z from 'zod';
import { isValidEmail } from 'utils/Helper';

const PERSON_NAME_REGEX = /^[A-Za-z .'-]+$/;

const createPersonNameSchema = (label: 'First name' | 'Last name') =>
  z
    .string()
    .min(1, `${label} is required.`)
    .min(3, `${label} must be at least 3 characters long.`)
    .max(50, `${label} cannot exceed 50 characters.`)
    .regex(
      PERSON_NAME_REGEX,
      `${label} can contain letters, spaces, hyphens, apostrophes, and dots only.`,
    );

export const SystemUserFormSchema = z.object({
  firstName: z
    .string()
    .min(3, 'First name must be at least 3 characters')
    .max(100, 'First name must be less than 100 characters')
    .regex(
      /^[A-Za-z\s]+$/,
      'First name cannot contain numbers or special characters',
    ),
  lastName: z
    .string()
    .min(3, 'Last name must be at least 3 characters')
    .max(100, 'Last name must be less than 100 characters')
    .regex(
      /^[A-Za-z\s]+$/,
      'Last name cannot contain numbers or special characters',
    ),
  email: z.email('Please enter a valid email address'),
  companyName: z.string().min(2, 'Organization name is required'),
  designation: z
    .string()
    .min(2, 'Designation is required')
    .max(100, 'Designation must be less than 100 characters')
    .regex(
      /^[A-Za-z\s]+$/,
      'Designation cannot contain numbers or special characters',
    ),
  department: z.string().min(1, 'Please select a department'),
  country: z.string().min(1, 'Please select a country'),
  supervisorName: z
    .string()
    .min(3, 'Supervisor name must be at least 3 characters')
    .max(100, 'Supervisor name must be less than 100 characters')
    .regex(
      /^[A-Za-z\s]+$/,
      'Supervisor name cannot contain numbers or special characters',
    ),
  roleIds: z.array(z.string()).min(1, 'Please select a role'),
  status: z.enum(Status),
});

export type TSystemUserFormFields = z.infer<typeof SystemUserFormSchema>;
export type TSystemUserFormFieldKeys = keyof TSystemUserFormFields;

export const DefaultSystemUserFormValues: TSystemUserFormFields = {
  firstName: '',
  lastName: '',
  email: '',
  companyName: '',
  designation: '',
  department: '',
  country: '',
  supervisorName: '',
  roleIds: [],
  status: Status.ACTIVE,
};

export const SystemUserFormFields = [
  {
    name: 'First Name',
    key: 'firstName',
    placeholder: 'First name',
    required: true,
  },
  {
    name: 'Last Name',
    key: 'lastName',
    placeholder: 'Last name',
    required: true,
  },
  { name: 'Email', key: 'email', placeholder: 'Email', required: true },
  {
    name: 'Organization Name',
    key: 'companyName',
    placeholder: 'Organization name',
    required: true,
  },
  {
    name: 'Designation/Job Title',
    key: 'designation',
    placeholder: 'Designation/Job Title',
    required: true,
  },
  {
    name: 'Department',
    key: 'department',
    placeholder: 'Select Department',
    required: true,
  },
  {
    name: 'Country',
    key: 'country',
    placeholder: 'Select Country',
    required: true,
  },
  {
    name: 'Supervisor Name',
    key: 'supervisorName',
    placeholder: 'Supervisor name',
    required: true,
  },
  {
    name: 'Role',
    key: 'roleIds',
    placeholder: 'Select Role',
    required: true,
  },
  {
    name: 'Status',
    key: 'status',
    placeholder: 'Select Status',
    required: true,
  },
];

export const userSchema = z.object({
  firstName: createPersonNameSchema('First name'),
  lastName: createPersonNameSchema('Last name'),
  email: z.string().min(1, 'Email is required.').max(254, 'Email must be less than 254 characters').refine(email => isValidEmail(email), {
    message: 'Invalid email address',
  }),
  phoneNumber: z
    .string()
    .min(1, 'Phone number is required.')
    .superRefine((value, ctx) => {
      if (!/^\d+$/.test(value)) {
        ctx.addIssue({
          code: 'custom',
          message: 'Phone number can contain numbers only.',
        });
        return;
      }
      if (value.length < 7) {
        ctx.addIssue({
          code: 'custom',
          message: 'Phone number must be at least 7 digits long.',
        });
        return;
      }
      if (value.length > 15) {
        ctx.addIssue({
          code: 'custom',
          message: 'Phone number cannot exceed 15 digits.',
        });
      }
    }),
  department: z.string().min(2, 'Please select a department'),
  status: z.enum(Status),
});

export type UserFormData = z.infer<typeof userSchema>;
