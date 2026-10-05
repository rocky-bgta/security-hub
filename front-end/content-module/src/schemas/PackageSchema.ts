import { Status } from 'models/Global';
import { array, object, string, z } from 'zod';

export const PackageFormSchema = object({
  packageName: string()
    .min(3, 'Package title is required')
    .max(100, 'Package title cannot exceed 100 characters')
    .regex(
      /^(?![_\-0-9])[A-Za-z0-9 _\-\(\)]*$/,
      'First character cannot be underscore, hyphen, or number; only alphanumeric characters, spaces, hyphens, underscores, and parentheses are allowed',
    )
    .trim(),
  packageStatus: string().min(3, 'Package status is required'),
  packageDescription: string().optional(),
  price: z.number().min(1, 'Package price is required'),
  courseIds: array(
    object({
      id: string(),
      courseName: string(),
      courseDescription: string(),
    }),
  ).optional(),
  featureIds: array(
    object({
      id: string(),
      featureName: string(),
      featureDescription: string(),
    }),
  ).optional(),
});

export type TPackageFormFields = z.infer<typeof PackageFormSchema>;

export const DefaultPackageFormValues = {
  packageName: '',
  packageStatus: Status.ENABLED,
  packageDescription: '',
  price: 0,
  courseIds: [],
  featureIds: [],
};
