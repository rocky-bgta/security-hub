import { Status } from 'models/Global';
import { object, string, boolean, z } from 'zod';

export const CertificateTemplateFormSchema = object({
  templateName: string()
    .min(2, 'Template name must be at least 2 characters')
    .max(100, 'Template name cannot exceed 100 characters')
    .trim(),
  certificateTitle: string()
    .min(2, 'Certificate title must be at least 2 characters')
    .max(200, 'Certificate title cannot exceed 200 characters')
    .trim(),
  certificateType: string()
    .min(2, 'Certificate type must be at least 2 characters')
    .max(100, 'Certificate type cannot exceed 100 characters')
    .trim(),
  acknowledgement: string()
    .min(2, 'Acknowledgement must be at least 2 characters')
    .max(500, 'Acknowledgement cannot exceed 500 characters')
    .trim(),
  completionStatus: string()
    .min(2, 'Completion status must be at least 2 characters')
    .max(100, 'Completion status cannot exceed 100 characters')
    .trim(),
  completionTitle: string()
    .min(2, 'Completion title must be at least 2 characters')
    .max(200, 'Completion title cannot exceed 200 characters')
    .trim(),

  signerName: string()
    .min(2, 'Signer name must be at least 2 characters')
    .max(100, 'Signer name cannot exceed 100 characters')
    .trim(),
  signerDesignation: string()
    .min(2, 'Signer designation must be at least 2 characters')
    .max(100, 'Signer designation cannot exceed 100 characters')
    .trim(),
  signatureIdentity: string().optional(),
  backgroundImageUrl: string().min(1, 'Background image is required'),
  logoImageUrl: string().min(1, 'Logo image is required'),
  signatureImageUrl: string().min(1, 'Signature image is required'),
  dynamicFields: object({
    showLearnerName: boolean(),
    showCourseName: boolean(),
    showIssueDate: boolean(),
    showCertificateId: boolean(),
    showQrCode: boolean(),
  }),
  status: string().min(1, 'Status is required'),
  isDefault: boolean(),
  isTrialTemplate: boolean(),
});

export type TCertificateTemplateFormFields = z.infer<
  typeof CertificateTemplateFormSchema
>;

export const DefaultCertificateTemplateFormValues: TCertificateTemplateFormFields =
  {
    templateName: '',
    certificateTitle: '',
    certificateType: '',
    acknowledgement: '',
    completionStatus: '',
    completionTitle: '',
    logoImageUrl: '',
    signatureImageUrl: '',
    signerName: '',
    signerDesignation: '',
    signatureIdentity: '',
    backgroundImageUrl: '',
    dynamicFields: {
      showLearnerName: true,
      showCourseName: true,
      showIssueDate: true,
      showCertificateId: true,
      showQrCode: true,
    },
    status: Status.ENABLED,
    isDefault: false,
    isTrialTemplate: false,
  };
