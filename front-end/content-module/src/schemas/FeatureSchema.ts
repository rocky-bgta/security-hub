import { Status } from 'models/Global';
import { object, string, z } from 'zod';

export const FeatureFormSchema = object({
  featureName: string()
    .min(2, 'Feature title is required')
    .max(100, 'Feature title cannot exceed 100 characters')
    .trim(),
  featureStatus: string().min(3, 'Feature status is required'),
  featureDescription: string().optional(),
  availability: string().min(1, 'Feature availability is required'),
});

export type TFeatureFormFields = z.infer<typeof FeatureFormSchema>;

export const DefaultFeatureFormValues = {
  featureName: '',
  featureStatus: Status.ENABLED,
  featureDescription: '',
  availability: 'FREE',
};
