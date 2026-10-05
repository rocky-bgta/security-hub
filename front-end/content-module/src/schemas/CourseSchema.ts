import { Status } from 'models/Global';
import { array, object, string, z } from 'zod';

// Schema for individual select option
const SelectOptionSchema = object({
  id: string(),
  label: string(),
  value: string(),
});

// Schema for product-package pair
const ProductPackagePairSchema = object({
  id: string(),
  productId: string().min(1, 'Product is required'),
  packageIds: array(string()).min(1, 'At least one package must be selected'),
  productName: string().min(1, 'Product name is required'),
});

export const CourseFormSchema = object({
  topicName: string()
    .min(3, 'Topic Name is required')
    .max(100, 'Topic Name cannot exceed 100 characters')
    .regex(
      /^(?![_\-0-9])[A-Za-z0-9 _\-\(\)]*$/,
      'First character cannot be underscore, hyphen, or number; only alphanumeric characters, spaces, hyphens, underscores, and parentheses are allowed',
    )
    .trim(),

  // courseStatus: string().min(3, 'Topic status is required'),

  compliance: array(SelectOptionSchema).min(
    1,
    'At least one compliance must be selected',
  ),

  country: array(SelectOptionSchema).min(
    1,
    'At least one country must be selected',
  ),

  category: array(SelectOptionSchema).min(
    1,
    'At least one category must be selected',
  ),

  contentType: string().min(2, 'Content type is required'),

  duration: string().min(1, 'Duration is required'),

  topicDescription: string().optional(),
  status: string().min(1, 'Status is required'),

  // New field for multiple product-package pairs
  // productPackages: array(ProductPackagePairSchema)
  //   .min(1, 'At least one product with packages must be selected')
  //   .refine(
  //     pairs =>
  //       pairs.every(pair => pair.productId && pair.packageIds.length > 0),
  //     'All product selections must have at least one package selected',
  //   ),
});

export type TCourseFormFields = z.infer<typeof CourseFormSchema>;

export const DefaultCourseFormValues: TCourseFormFields = {
  topicName: '',
  topicDescription: '',
  compliance: [],
  category: [],
  country: [],
  contentType: '',
  duration: '',
  status: Status.ENABLED,
  // courseStatus: '',
  // productPackages: [],
};
