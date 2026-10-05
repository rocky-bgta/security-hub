import { object, string, z } from 'zod';

export const ChangePasswordSchema = object({
  OldPassword: string()
    .nonempty('Current password is required')
    .min(8, 'Current password must be at least 8 characters')
    .max(64, 'Current password cannot exceed 64 characters')
    .trim(),
  NewPassword: string()
    .nonempty('New password is required')
    .min(8, 'New password must be at least 8 characters')
    .max(64, 'New password cannot exceed 64 characters')
    .regex(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
      'Password must contain at least one uppercase letter, one lowercase letter, one number and one special character',
    )
    .trim(),
  ConfirmPassword: string()
    .nonempty('Confirm password is required')
    .min(8, 'Confirm password must be at least 8 characters')
    .max(64, 'Confirm password cannot exceed 64 characters')
    .trim(),
})
  .refine(data => data.NewPassword === data.ConfirmPassword, {
    path: ['ConfirmPassword'],
    message: 'Passwords do not match',
  })
  .refine(data => data.OldPassword !== data.NewPassword, {
    path: ['NewPassword'],
    message: 'New password must be different from current password',
  });

export type TChangePasswordFormFields = z.infer<typeof ChangePasswordSchema>;
export type TChangePasswordFormFieldsKeys = keyof TChangePasswordFormFields;

export const DefaultChangePasswordFormValues = {
  OldPassword: '',
  NewPassword: '',
  ConfirmPassword: '',
};

export const ChangePasswordFormFields = [
  {
    key: 'OldPassword',
    name: 'Current Password',
    type: 'password',
    placeholder: 'Enter your current password',
    required: true,
  },
  {
    key: 'NewPassword',
    name: 'New Password',
    type: 'password',
    placeholder: 'Enter a new password',
    required: true,
  },
  {
    key: 'ConfirmPassword',
    name: 'Confirm New Password',
    type: 'password',
    placeholder: 'Re-enter your new password',
    required: true,
  },
];
