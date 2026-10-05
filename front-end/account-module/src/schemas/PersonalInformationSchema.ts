import { object, string, z } from 'zod';

import { isValidEmail } from 'utils/Helper';

const CITY_REGEX = /^[A-Za-z\s\-']+$/;
const ZIP_POSTAL_CODE_REGEX = /^[A-Za-z0-9\s\-]+$/;
const STREET_ADDRESS_REGEX = /^[A-Za-z0-9\s,.\-#\/]+$/;

const requiredField = () => string().nonempty('This field is required');

const validateCity = (val: string, ctx: z.RefinementCtx) => {
  if (!val.trim()) {
    ctx.addIssue({ code: 'custom', message: 'City is required.' });
    return;
  }
  if (val.length < 2) {
    ctx.addIssue({
      code: 'custom',
      message: 'City must be at least 2 characters long.',
    });
    return;
  }
  if (val.length > 100) {
    ctx.addIssue({
      code: 'custom',
      message: 'City cannot exceed 100 characters.',
    });
    return;
  }
  if (!CITY_REGEX.test(val)) {
    ctx.addIssue({
      code: 'custom',
      message: 'City can contain letters, spaces, hyphens, and apostrophes only.',
    });
  }
};

const validateZipPostalCode = (val: string, ctx: z.RefinementCtx) => {
  if (!val.trim()) {
    ctx.addIssue({
      code: 'custom',
      message: 'Zip or postal code is required.',
    });
    return;
  }
  if (val.length > 20) {
    ctx.addIssue({
      code: 'custom',
      message: 'Zip or postal code cannot exceed 20 characters.',
    });
    return;
  }
  if (val.length < 3 || !ZIP_POSTAL_CODE_REGEX.test(val)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Please enter a valid zip or postal code.',
    });
  }
};

const validateStreetAddress = (
  val: string,
  ctx: z.RefinementCtx,
  required = true,
) => {
  if (!val.trim()) {
    if (required) {
      ctx.addIssue({
        code: 'custom',
        message: 'Street address is required.',
      });
    }
    return;
  }
  if (val.length < 5) {
    ctx.addIssue({
      code: 'custom',
      message: 'Street address must be at least 5 characters long.',
    });
    return;
  }
  if (val.length > 255) {
    ctx.addIssue({
      code: 'custom',
      message: 'Street address cannot exceed 255 characters.',
    });
    return;
  }
  if (!STREET_ADDRESS_REGEX.test(val)) {
    ctx.addIssue({
      code: 'custom',
      message: 'Street address contains unsupported characters.',
    });
  }
};

export const PersonalInformationSchema = object({
  organizationName: requiredField(),
  organizationType: requiredField(),
  contactEmail: string()
    .nonempty('This field is required')
    .refine(isValidEmail, 'Please enter a valid email address'),
  phoneNumber: requiredField(),
  country: requiredField(),
  stateProvince: requiredField(),
  industry: requiredField(),
  organizationSize: requiredField(),
  streetAddress: string().superRefine((val, ctx) =>
    validateStreetAddress(val, ctx, true),
  ),
  streetAddressLine2: string().superRefine((val, ctx) =>
    validateStreetAddress(val, ctx, false),
  ),
  city: string().superRefine(validateCity),
  zipPostalCode: string().superRefine(validateZipPostalCode),
});

export type TPersonalInformationFormFields = z.infer<
  typeof PersonalInformationSchema
>;

export const DefaultPersonalInformationFormValues: TPersonalInformationFormFields =
  {
    organizationName: '',
    organizationType: '',
    contactEmail: '',
    phoneNumber: '',
    country: '',
    stateProvince: '',
    industry: '',
    organizationSize: '',
    streetAddress: '',
    streetAddressLine2: '',
    city: '',
    zipPostalCode: '',
  };
