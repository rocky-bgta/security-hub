import z from 'zod';

export const tierSchema = z.object({
  tierName: z
    .string()
    .min(2, 'Tier name is required')
    .max(100, 'Tier name must be less than 100 characters'),
  tierDescription: z
    .string()
    .max(500, 'Description must be less than 500 characters')
    .optional(),
  commissionPercentage: z
    .number()
    .min(5, 'Commission percentage must be at least 5')
    .max(100, 'Commission percentage cannot exceed 100'),
  salesThreshold: z.number().min(0, 'Sales threshold must be at least 0'),
  eligibilityCriteria: z
    .string()
    .max(500, 'Eligibility criteria must be less than 500 characters')
    .optional(),
  tierBenefits: z
    .string()
    .max(500, 'Tier benefits must be less than 500 characters')
    .optional(),
  active: z.string(),
});

export type TierFormData = z.infer<typeof tierSchema>;
