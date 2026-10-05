import { Status } from 'models/Global';
import { array, object, string, z } from 'zod';

export const ProductFormSchema = object({
  productName: string()
    .min(3, 'Product title is required')
    .max(100, 'Product title cannot exceed 100 characters')
    .regex(
      /^(?![_\-0-9])[A-Za-z0-9 _\-\(\)]*$/,
      'First character cannot be underscore, hyphen, or number; only alphanumeric characters, spaces, hyphens, underscores, and parentheses are allowed',
    )
    .trim(),
  productStatus: string().min(3, 'Product status is required'),
  productDescription: string().optional(),
  courseIds: array(
    object({
      id: string(),
      courseName: string(),
      courseDescription: string(),
    }),
  ).optional(),
});

export type TProductFormFields = z.infer<typeof ProductFormSchema>;
export type TProductFormFieldKeys = keyof TProductFormFields;

export const DefaultProductFormValues = {
  productName: '',
  productStatus: Status.ENABLED,
  productDescription: '',
  courseIds: [],
};

export const ProductFormFields = [
  {
    key: 'productName',
    name: 'Product Name',
    type: 'input',
    required: true,
    placeholder: 'Product Title',
  },
  {
    key: 'productStatus',
    name: 'Product Status',
    type: 'select',
    required: true,
  },
  {
    key: 'productDescription',
    name: 'Product Description (optional)',
    type: 'textarea',
    placeholder: 'Product Description',
  },
];
