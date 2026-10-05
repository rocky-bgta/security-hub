import { object, string, z } from 'zod';

const TITLE_REGEX = /^[A-Za-z0-9\s.,!?'":;()\-/&]+$/;
const LEADER_NAME_REGEX = /^[A-Za-z\s\-']+$/;
const DESIGNATION_REGEX = /^[A-Za-z0-9\s\-&]+$/;

const validateTitle = (val: string, ctx: z.RefinementCtx) => {
  const trimmed = val.trim();
  if (!trimmed) {
    ctx.addIssue({ code: 'custom', message: 'Title is required.' });
    return;
  }
  if (trimmed.length < 5) {
    ctx.addIssue({
      code: 'custom',
      message: 'Title must be at least 5 characters long.',
    });
    return;
  }
  if (trimmed.length > 150) {
    ctx.addIssue({
      code: 'custom',
      message: 'Title cannot exceed 150 characters.',
    });
    return;
  }
  if (!TITLE_REGEX.test(trimmed)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Title contains unsupported characters.',
    });
  }
};

const validateLeaderName = (val: string, ctx: z.RefinementCtx) => {
  const trimmed = val.trim();
  if (!trimmed) {
    ctx.addIssue({ code: 'custom', message: 'Leader name is required.' });
    return;
  }
  if (trimmed.length < 3) {
    ctx.addIssue({
      code: 'custom',
      message: 'Leader name must be at least 3 characters long.',
    });
    return;
  }
  if (trimmed.length > 50) {
    ctx.addIssue({
      code: 'custom',
      message: 'Leader name cannot exceed 50 characters.',
    });
    return;
  }
  if (!LEADER_NAME_REGEX.test(trimmed)) {
    ctx.addIssue({
      code: 'custom',
      message:
        'Leader name can contain letters, spaces, hyphens, and apostrophes only.',
    });
  }
};

const validateDesignation = (val: string, ctx: z.RefinementCtx) => {
  const trimmed = val.trim();
  if (!trimmed) {
    ctx.addIssue({ code: 'custom', message: 'Designation is required.' });
    return;
  }
  if (trimmed.length < 2) {
    ctx.addIssue({
      code: 'custom',
      message: 'Designation must be at least 2 characters long.',
    });
    return;
  }
  if (trimmed.length > 250) {
    ctx.addIssue({
      code: 'custom',
      message: 'Designation cannot exceed 250 characters.',
    });
    return;
  }
  if (!DESIGNATION_REGEX.test(trimmed)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Designation contains unsupported characters.',
    });
  }
};

export const LeaderboardFormSchema = object({
  title: string().superRefine(validateTitle),
  name: string().superRefine(validateLeaderName),
  designation: string().superRefine(validateDesignation),
});

export type TLeaderboardFormFields = z.infer<typeof LeaderboardFormSchema>;

export const DefaultLeaderboardFormValues: TLeaderboardFormFields = {
  title: '',
  name: '',
  designation: '',
};

export type TLeaderboardFieldErrors = Partial<
  Record<keyof TLeaderboardFormFields | 'thumbnail' | 'video', string>
>;

export const validateLeaderboardTextFields = (
  data: TLeaderboardFormFields,
): TLeaderboardFieldErrors => {
  const result = LeaderboardFormSchema.safeParse(data);
  if (result.success) return {};

  const fieldErrors: TLeaderboardFieldErrors = {};
  for (const issue of result.error.issues) {
    const field = issue.path[0] as keyof TLeaderboardFormFields;
    if (field && !fieldErrors[field]) {
      fieldErrors[field] = issue.message;
    }
  }
  return fieldErrors;
};

export const LEADERBOARD_FIELD_MAX_LENGTH = {
  title: 150,
  name: 50,
  designation: 250,
} as const;
