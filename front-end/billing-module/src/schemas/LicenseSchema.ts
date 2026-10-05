import { object, string, z } from 'zod';

export const LicenseFormSchema = object({
  organization: string().min(2, 'Product is required'),
  product: string().min(1, 'Product is required'),
  package: string().min(1, 'Package is required'),
  numberOfUsers: string().min(2, 'Number of user is required'),
  licenseValidity: string().min(1, 'License Validity is required'),
  startDate: string().min(1, 'Start Date is required'),
});

export type TLicenseFormFields = z.infer<typeof LicenseFormSchema>;
export type TLicenseFormFieldKeys = keyof TLicenseFormFields;

export const DefaultLicenseFormValues = {
  organization: '',
  product: '',
  package: '',
  numberOfUsers: '',
  licenseValidity: '',
  startDate: '',
};

export const LicenseFormFields = [
  {
    name: 'Organization',
    key: 'organization',
    required: true,
  },
  {
    name: 'Product',
    key: 'product',
    required: true,
  },
  {
    name: 'Package',
    key: 'package',
    required: true,
  },
  {
    name: 'Number of user',
    key: 'numberOfUsers',
    placeholder: 'Number of user',
    required: true,
  },
  {
    name: 'License Validity',
    key: 'licenseValidity',
    required: true,
  },
  {
    name: 'Start Date',
    key: 'startDate',
    placeholder: 'Start Date',
    required: true,
  },
];
