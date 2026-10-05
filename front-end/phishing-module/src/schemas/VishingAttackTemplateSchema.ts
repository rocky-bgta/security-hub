import { z } from 'zod';

export const VishingAttackTemplateFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, 'Template name is required')
    .max(150, 'Template name must be 150 characters or less'),
  script: z.string().trim().min(1, 'Script is required'),
});

export type TVishingAttackTemplateForm = z.infer<
  typeof VishingAttackTemplateFormSchema
>;

export const defaultVishingAttackTemplateForm: TVishingAttackTemplateForm = {
  name: '',
  script: '',
};
