import { Status } from 'models/Global';
import { object, string, z } from 'zod';

export const ChapterFormSchema = object({
  chapterName: string()
    .min(3, 'Chapter title is required')
    .max(100, 'Chapter title cannot exceed 100 characters')
    .regex(
      /^(?![_\-0-9])[A-Za-z0-9 _\-\(\)]*$/,
      'First character cannot be underscore, hyphen, or number; only alphanumeric characters, spaces, hyphens, underscores, and parentheses are allowed',
    )
    .trim(),
  chapterStatus: string().min(3, 'Chapter status is required'),
  chapterDescription: string().optional(),
});

export type TChapterFormFields = z.infer<typeof ChapterFormSchema>;
export type TChapterFormFieldsKeys = keyof TChapterFormFields;

export const DefaultChapterFormValues = {
  chapterName: '',
  chapterStatus: Status.ENABLED,
  chapterDescription: '',
};

export const ChapterFormFields = [
  { key: 'chapterName', name: 'Chapter Name', type: 'text', required: true },
  {
    key: 'chapterDescription',
    name: 'Description (optional)',
    type: 'textarea',
    required: false,
  },
  {
    key: 'chapterStatus',
    name: 'Status',
    type: 'select',
    required: true,
  },
];
