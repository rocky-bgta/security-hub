import { object, string, z } from 'zod';

export const AddRoleFormSchema = object({
  name: string()
    .min(1, 'Role name is required')
    .regex(/^([a-zA-Z\s*.]{2,})$/, {
      message:
        'Role Name must contain at least two characters and only letters, spaces, and dots.',
    }),
  status: string().min(1, 'Please select a country'),
});

export type TAddRoleFormFields = z.infer<typeof AddRoleFormSchema>;
export type TAddRoleFormFieldKeys = keyof TAddRoleFormFields;

export const DefaultAddRoleFormValues = {
  name: '',
  status: '',
};

export const AddRoleFormFields = [
  {
    name: 'Role Name',
    key: 'name',
    placeholder: 'Role name',
  },
  {
    name: 'Status',
    key: 'status',
    placeholder: 'Select status',
  },
];
