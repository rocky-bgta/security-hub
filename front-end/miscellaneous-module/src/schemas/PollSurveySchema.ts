import { array, boolean, object, string, z } from 'zod';

const HTML_OR_SCRIPT_REGEX = /<\s*\/?[a-z][^>]*>|<\s*script\b/i;
const POLL_SURVEY_TITLE_REGEX = /^[A-Za-z0-9\s\-'.(),]+$/;
const TEXT_CONTENT_REGEX = /^[A-Za-z0-9\s.,!?'"():;\-/&\n\r]+$/;

export const POLL_SURVEY_FIELD_MAX_LENGTH = {
  title: 100,
  minTitle: 10,
  description: 500,
  question: 500,
  minQuestion: 5,
  option: 100,
  maxOptions: 10,
  minOptions: 2,
} as const;

export const POLL_SURVEY_TYPES = ['POLL', 'SURVEY'] as const;
export const POLL_SURVEY_STATUSES = ['DRAFT', 'ACTIVE', 'INACTIVE'] as const;
export const QUESTION_TYPES = ['RADIO', 'MCQ'] as const;

const hasHtmlOrScript = (val: string): boolean => HTML_OR_SCRIPT_REGEX.test(val);

export const parseLocalDate = (dateStr: string): Date | null => {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return null;
  const [year, month, day] = dateStr.split('-').map(Number);
  const date = new Date(year, month - 1, day);
  if (
    date.getFullYear() !== year ||
    date.getMonth() !== month - 1 ||
    date.getDate() !== day
  ) {
    return null;
  }
  return date;
};

export const getTodayLocal = (): Date => {
  const now = new Date();
  return new Date(now.getFullYear(), now.getMonth(), now.getDate());
};

export const getTodayISO = (): string => {
  const today = getTodayLocal();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const validateTitle = (val: string, ctx: z.RefinementCtx) => {
  const trimmed = val.trim();
  if (!trimmed) {
    ctx.addIssue({ code: 'custom', message: 'Title is required.' });
    return;
  }
  if (trimmed.length < POLL_SURVEY_FIELD_MAX_LENGTH.minTitle) {
    ctx.addIssue({
      code: 'custom',
      message: 'Title must be at least 10 characters long.',
    });
    return;
  }
  if (trimmed.length > POLL_SURVEY_FIELD_MAX_LENGTH.title) {
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
  if (!POLL_SURVEY_TITLE_REGEX.test(trimmed)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Title contains unsupported characters.',
    });
  }
};

const validateDescription = (val: string, ctx: z.RefinementCtx) => {
  if (!val.trim()) return;

  if (val.length > POLL_SURVEY_FIELD_MAX_LENGTH.description) {
    ctx.addIssue({
      code: 'custom',
      message: 'Description cannot exceed 500 characters.',
    });
    return;
  }
  if (hasHtmlOrScript(val)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Description contains unsupported content or characters.',
    });
    return;
  }
  if (!TEXT_CONTENT_REGEX.test(val)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Description contains unsupported content or characters.',
    });
  }
};

const validateQuestionText = (val: string, ctx: z.RefinementCtx) => {
  const trimmed = val.trim();
  if (!trimmed) {
    ctx.addIssue({ code: 'custom', message: 'Question text is required.' });
    return;
  }
  if (trimmed.length < POLL_SURVEY_FIELD_MAX_LENGTH.minQuestion) {
    ctx.addIssue({
      code: 'custom',
      message: 'Question must be at least 5 characters long.',
    });
    return;
  }
  if (trimmed.length > POLL_SURVEY_FIELD_MAX_LENGTH.question) {
    ctx.addIssue({
      code: 'custom',
      message: 'Question cannot exceed 500 characters.',
    });
    return;
  }
  if (hasHtmlOrScript(trimmed)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Question contains unsupported content or characters.',
    });
    return;
  }
  if (!TEXT_CONTENT_REGEX.test(trimmed)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Question contains unsupported content or characters.',
    });
  }
};

const validateOptions = (options: string[], ctx: z.RefinementCtx) => {
  const trimmedOptions = options.map(opt => opt.trim()).filter(Boolean);

  if (trimmedOptions.length < POLL_SURVEY_FIELD_MAX_LENGTH.minOptions) {
    ctx.addIssue({
      code: 'custom',
      message: 'Please add at least two answer options.',
      path: [],
    });
    return;
  }

  if (trimmedOptions.length > POLL_SURVEY_FIELD_MAX_LENGTH.maxOptions) {
    ctx.addIssue({
      code: 'custom',
      message: 'A maximum of 10 answer options is allowed.',
      path: [],
    });
    return;
  }

  const normalized = trimmedOptions.map(opt => opt.toLowerCase());
  const seen = new Set<string>();
  for (const opt of normalized) {
    if (seen.has(opt)) {
      ctx.addIssue({
        code: 'custom',
        message: 'Duplicate answer options are not allowed.',
        path: [],
      });
      return;
    }
    seen.add(opt);
  }

  options.forEach((option, index) => {
    const trimmed = option.trim();
    if (!trimmed) return;

    if (trimmed.length > POLL_SURVEY_FIELD_MAX_LENGTH.option) {
      ctx.addIssue({
        code: 'custom',
        message: 'Answer option cannot exceed 100 characters.',
        path: [index],
      });
      return;
    }

    if (hasHtmlOrScript(trimmed) || !TEXT_CONTENT_REGEX.test(trimmed)) {
      ctx.addIssue({
        code: 'custom',
        message: 'Answer option contains unsupported characters.',
        path: [index],
      });
    }
  });
};

const QuestionSchema = object({
  id: string(),
  type: string().superRefine((val, ctx) => {
    if (!val?.trim()) {
      ctx.addIssue({
        code: 'custom',
        message: 'Please select a question type.',
      });
      return;
    }
    if (!QUESTION_TYPES.includes(val as (typeof QUESTION_TYPES)[number])) {
      ctx.addIssue({
        code: 'custom',
        message: 'Please select a valid question type.',
      });
    }
  }),
  question: string().superRefine(validateQuestionText),
  options: array(string()).superRefine(validateOptions),
});

export const PollSurveyFormSchema = object({
  title: string().superRefine(validateTitle),
  description: string().superRefine(validateDescription),
  type: string().superRefine((val, ctx) => {
    if (!val?.trim()) {
      ctx.addIssue({ code: 'custom', message: 'Please select a type.' });
      return;
    }
    if (!POLL_SURVEY_TYPES.includes(val as (typeof POLL_SURVEY_TYPES)[number])) {
      ctx.addIssue({
        code: 'custom',
        message: 'Please select a valid type.',
      });
    }
  }),
  status: string().superRefine((val, ctx) => {
    if (!val?.trim()) {
      ctx.addIssue({ code: 'custom', message: 'Please select a status.' });
      return;
    }
    if (
      !POLL_SURVEY_STATUSES.includes(
        val as (typeof POLL_SURVEY_STATUSES)[number],
      )
    ) {
      ctx.addIssue({
        code: 'custom',
        message: 'Please select a valid status.',
      });
    }
  }),
  startDate: string().superRefine((val, ctx) => {
    if (!val?.trim()) {
      ctx.addIssue({ code: 'custom', message: 'Start date is required.' });
      return;
    }
    const parsed = parseLocalDate(val);
    if (!parsed) {
      ctx.addIssue({
        code: 'custom',
        message: 'Please select a valid start date.',
      });
    }
  }),
  endDate: string().superRefine((val, ctx) => {
    if (!val?.trim()) {
      ctx.addIssue({ code: 'custom', message: 'End date is required.' });
      return;
    }
    const parsed = parseLocalDate(val);
    if (!parsed) {
      ctx.addIssue({
        code: 'custom',
        message: 'Please select a valid end date.',
      });
    }
  }),
  showResults: boolean(),
  multipleSubmissions: boolean(),
  questions: array(QuestionSchema).min(
    1,
    'Please add at least one question before creating the poll or survey.',
  ),
});

export type TPollSurveyQuestion = z.infer<typeof QuestionSchema>;

export type TPollSurveyFormData = z.infer<typeof PollSurveyFormSchema>;

export type TQuestionFieldErrors = {
  questionText?: string;
  questionType?: string;
  options?: string;
  optionErrors?: Record<number, string>;
};

export type TPollSurveyFormErrors = {
  title?: string;
  type?: string;
  status?: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  form?: string;
  questions?: Record<string, TQuestionFieldErrors>;
};

export interface TValidatePollSurveyOptions {
  isEdit?: boolean;
  originalStartDate?: string;
}

const applyDateCrossFieldValidation = (
  data: TPollSurveyFormData,
  errors: TPollSurveyFormErrors,
  options?: TValidatePollSurveyOptions,
) => {
  const startParsed = parseLocalDate(data.startDate);
  const endParsed = parseLocalDate(data.endDate);

  if (startParsed && !errors.startDate) {
    const today = getTodayLocal();
    const isUnchangedPastDate =
      options?.isEdit &&
      options.originalStartDate &&
      data.startDate === options.originalStartDate;

    if (!isUnchangedPastDate && startParsed < today) {
      errors.startDate = 'Start date cannot be earlier than today.';
    }
  }

  if (startParsed && endParsed && !errors.endDate && !errors.startDate) {
    if (endParsed <= startParsed) {
      errors.endDate = 'End date must be later than the start date.';
    }
  }
};

export const hasPollSurveyFormErrors = (
  errors: TPollSurveyFormErrors,
): boolean => {
  const topLevelKeys: Array<keyof TPollSurveyFormErrors> = [
    'title',
    'type',
    'status',
    'description',
    'startDate',
    'endDate',
    'form',
  ];

  if (topLevelKeys.some(key => Boolean(errors[key]))) return true;

  if (!errors.questions) return false;

  return Object.values(errors.questions).some(questionError => {
    if (
      questionError.questionText ||
      questionError.questionType ||
      questionError.options
    ) {
      return true;
    }
    return Object.values(questionError.optionErrors ?? {}).some(Boolean);
  });
};

export const validatePollSurveyForm = (
  data: TPollSurveyFormData,
  options?: TValidatePollSurveyOptions,
): TPollSurveyFormErrors => {
  const result = PollSurveyFormSchema.safeParse(data);
  const errors: TPollSurveyFormErrors = {};

  if (!result.success) {
    for (const issue of result.error.issues) {
      const [root, questionIndex, field, optionIndex] = issue.path;

      if (root === 'questions' && typeof questionIndex === 'number') {
        const question = data.questions[questionIndex];
        if (!question) continue;

        if (!errors.questions) errors.questions = {};
        if (!errors.questions[question.id]) {
          errors.questions[question.id] = {};
        }

        const questionErrors = errors.questions[question.id];

        if (field === 'question' && !questionErrors.questionText) {
          questionErrors.questionText = issue.message;
        } else if (field === 'type' && !questionErrors.questionType) {
          questionErrors.questionType = issue.message;
        } else if (field === 'options') {
          if (typeof optionIndex === 'number') {
            if (!questionErrors.optionErrors) {
              questionErrors.optionErrors = {};
            }
            if (!questionErrors.optionErrors[optionIndex]) {
              questionErrors.optionErrors[optionIndex] = issue.message;
            }
          } else if (!questionErrors.options) {
            questionErrors.options = issue.message;
          }
        }
        continue;
      }

      if (typeof root === 'string') {
        const fieldName = root as keyof TPollSurveyFormErrors;
        if (
          fieldName === 'title' ||
          fieldName === 'type' ||
          fieldName === 'status' ||
          fieldName === 'description' ||
          fieldName === 'startDate' ||
          fieldName === 'endDate'
        ) {
          if (!errors[fieldName]) {
            errors[fieldName] = issue.message;
          }
        } else if (root === 'questions' && !errors.form) {
          errors.form = issue.message;
        }
      }
    }
  }

  applyDateCrossFieldValidation(data, errors, options);

  return errors;
};
