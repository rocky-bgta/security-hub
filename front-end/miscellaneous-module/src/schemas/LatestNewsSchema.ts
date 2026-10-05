import { object, string, z } from 'zod';

import { validateThumbnailFile } from 'features/leader-board/leaderboardValidation';

const HTML_OR_SCRIPT_REGEX = /<\s*\/?[a-z][^>]*>|<\s*script\b/i;
const TITLE_REGEX = /^[A-Za-z0-9\s.,!?'":;()\-/&]+$/;
const SLUG_REGEX = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
const DATETIME_LOCAL_REGEX = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/;

export const LATEST_NEWS_FIELD_MAX_LENGTH = {
  title: 100,
  minTitle: 5,
  slug: 200,
  minSlug: 3,
  content: 50000,
} as const;

export const LATEST_NEWS_STATUSES = ['DRAFT', 'ACTIVE', 'INACTIVE'] as const;

const hasHtmlOrScript = (val: string): boolean => HTML_OR_SCRIPT_REGEX.test(val);

export const isRichTextEmpty = (html: string): boolean => {
  if (!html || !html.trim()) return true;

  const stripped = html
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/gi, ' ')
    .replace(/\u00a0/g, ' ')
    .trim();

  return stripped.length === 0;
};

export const parseDateTimeLocal = (value: string): Date | null => {
  if (!DATETIME_LOCAL_REGEX.test(value)) return null;

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return null;

  return date;
};

const validateTitle = (val: string, ctx: z.RefinementCtx) => {
  const trimmed = val.trim();
  if (!trimmed) {
    ctx.addIssue({ code: 'custom', message: 'Title is required.' });
    return;
  }
  if (trimmed.length < LATEST_NEWS_FIELD_MAX_LENGTH.minTitle) {
    ctx.addIssue({
      code: 'custom',
      message: 'Title must be at least 5 characters long.',
    });
    return;
  }
  if (trimmed.length > LATEST_NEWS_FIELD_MAX_LENGTH.title) {
    ctx.addIssue({
      code: 'custom',
      message: 'Title cannot exceed 100 characters.',
    });
    return;
  }
  if (hasHtmlOrScript(trimmed)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Title contains unsupported characters.',
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

const validateSlug = (val: string, ctx: z.RefinementCtx) => {
  const trimmed = val.trim();
  if (!trimmed) {
    ctx.addIssue({ code: 'custom', message: 'URL is required.' });
    return;
  }
  if (trimmed.length < LATEST_NEWS_FIELD_MAX_LENGTH.minSlug) {
    ctx.addIssue({
      code: 'custom',
      message: 'URL must be at least 3 characters long.',
    });
    return;
  }
  if (trimmed.length > LATEST_NEWS_FIELD_MAX_LENGTH.slug) {
    ctx.addIssue({
      code: 'custom',
      message: 'URL cannot exceed 200 characters.',
    });
    return;
  }
  if (!SLUG_REGEX.test(trimmed)) {
    ctx.addIssue({
      code: 'custom',
      message:
        'Please enter a valid URL slug (lowercase letters, numbers, and hyphens only).',
    });
  }
};

const validateContent = (val: string, ctx: z.RefinementCtx) => {
  if (isRichTextEmpty(val)) {
    ctx.addIssue({ code: 'custom', message: 'Content is required.' });
    return;
  }
  if (val.length > LATEST_NEWS_FIELD_MAX_LENGTH.content) {
    ctx.addIssue({
      code: 'custom',
      message: 'Content cannot exceed 50,000 characters.',
    });
  }
};

const validatePublishDate = (val: string, ctx: z.RefinementCtx) => {
  if (!val.trim()) {
    ctx.addIssue({ code: 'custom', message: 'Publish date is required.' });
    return;
  }
  if (!parseDateTimeLocal(val)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Please enter a valid publish date.',
    });
  }
};

const validateExpiryDate = (val: string, ctx: z.RefinementCtx) => {
  if (!val.trim()) {
    ctx.addIssue({ code: 'custom', message: 'Expiry date is required.' });
    return;
  }
  if (!parseDateTimeLocal(val)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Please enter a valid expiry date.',
    });
  }
};

export const LatestNewsFormSchema = object({
  title: string().superRefine(validateTitle),
  slug: string().superRefine(validateSlug),
  content: string().superRefine(validateContent),
  category: string().min(1, 'Category is required.'),
  status: z.enum(LATEST_NEWS_STATUSES, {
    message: 'Status is required.',
  }),
  publishDate: string().superRefine(validatePublishDate),
  expiryDate: string().superRefine(validateExpiryDate),
}).superRefine((data, ctx) => {
  const publishParsed = parseDateTimeLocal(data.publishDate);
  const expiryParsed = parseDateTimeLocal(data.expiryDate);

  if (publishParsed && expiryParsed && publishParsed >= expiryParsed) {
    ctx.addIssue({
      code: 'custom',
      message: 'Expiry date must be later than the publish date.',
      path: ['expiryDate'],
    });
  }
});

export type TLatestNewsFormData = z.infer<typeof LatestNewsFormSchema>;

export type TLatestNewsFormErrors = {
  title?: string;
  slug?: string;
  content?: string;
  category?: string;
  status?: string;
  publishDate?: string;
  expiryDate?: string;
  thumbnailImage?: string;
};

export interface TValidateLatestNewsOptions {
  thumbnailFile?: File | null;
  hasExistingThumbnail?: boolean;
}

export const hasLatestNewsFormErrors = (
  errors: TLatestNewsFormErrors,
): boolean => {
  return Object.values(errors).some(Boolean);
};

export const validateLatestNewsForm = (
  data: TLatestNewsFormData,
  options?: TValidateLatestNewsOptions,
): TLatestNewsFormErrors => {
  const result = LatestNewsFormSchema.safeParse(data);
  const errors: TLatestNewsFormErrors = {};

  if (!result.success) {
    for (const issue of result.error.issues) {
      const field = issue.path[0] as keyof TLatestNewsFormErrors;
      if (field && !errors[field]) {
        errors[field] = issue.message;
      }
    }
  }

  const { thumbnailFile, hasExistingThumbnail } = options ?? {};

  if (!hasExistingThumbnail && !thumbnailFile) {
    errors.thumbnailImage = 'Thumbnail image is required.';
  } else if (thumbnailFile) {
    const thumbnailError = validateThumbnailFile(thumbnailFile);
    if (thumbnailError) {
      errors.thumbnailImage = thumbnailError;
    }
  }

  return errors;
};
