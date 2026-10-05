import { zodResolver } from '@hookform/resolvers/zod';
import { InfoIcon, Upload } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';
import { useAPI } from 'hooks/UseAPI';
import { useUploader } from 'hooks/UseUploader';
import { ICertificateTemplateDetails } from 'models/Certificate';
import { FileType, IResponse, Status } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  CertificateTemplateFormSchema,
  DefaultCertificateTemplateFormValues,
  TCertificateTemplateFormFields,
} from 'schemas/CertificateTemplateSchema';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  id?: string;
  setIsOpen: (open: boolean) => void;
  onSubmit: () => void;
}

const DYNAMIC_FIELDS = [
  { key: 'showLearnerName', label: 'Learner Name' },
  { key: 'showCourseName', label: 'Course Name' },
  { key: 'showIssueDate', label: 'Issue Date' },
  { key: 'showCertificateId', label: 'Certificate ID' },
  { key: 'showQrCode', label: 'QR Code' },
] as const;

const TemplateCreateDialog = ({ isOpen, id, setIsOpen, onSubmit }: IProps) => {
  const {
    control,
    reset,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<TCertificateTemplateFormFields>({
    resolver: zodResolver(CertificateTemplateFormSchema),
    defaultValues: DefaultCertificateTemplateFormValues,
    mode: 'onChange',
  });

  const [submitting, setSubmitting] = useState<boolean>(false);
  const [backgroundImage, setBackgroundImage] = useState<{
    url: string;
    selectedFile: File | null;
    previewUrl: string | null;
  }>({ url: '', selectedFile: null, previewUrl: null });
  const [logoImage, setLogoImage] = useState<{
    url: string;
    selectedFile: File | null;
    previewUrl: string | null;
  }>({ url: '', selectedFile: null, previewUrl: null });
  const [signatureImage, setSignatureImage] = useState<{
    url: string;
    selectedFile: File | null;
    previewUrl: string | null;
  }>({ url: '', selectedFile: null, previewUrl: null });

  const apiClient = useAPI();
  const { uploadFile } = useUploader();
  const fileUploaderRef = useRef<FileUploaderHandle>(null);
  const logoUploaderRef = useRef<FileUploaderHandle>(null);
  const signatureUploaderRef = useRef<FileUploaderHandle>(null);

  useEffect(() => {
    if (isOpen && id) {
      fetchTemplate();
    }
  }, [isOpen, id]);

  const fetchTemplate = async () => {
    try {
      const response: IResponse<ICertificateTemplateDetails> =
        await apiClient.get(
          API_END_POINTS.GET_CERTIFICATE_TEMPLATE_DETAILS.replace(
            ':id',
            id as string,
          ),
        );
      if (isSuccessResponse(response.statusCode)) {
        reset(response.data);
        setBackgroundImage({
          url: response.data.backgroundImageUrl,
          selectedFile: null,
          previewUrl: null,
        });
        setLogoImage({
          url: response.data.logoImageUrl || '',
          selectedFile: null,
          previewUrl: null,
        });
        setSignatureImage({
          url: response.data.signatureImageUrl || '',
          selectedFile: null,
          previewUrl: null,
        });
        // Update form values for validation
        setValue('backgroundImageUrl', response.data.backgroundImageUrl || '');
        setValue('logoImageUrl', response.data.logoImageUrl || '');
        setValue('signatureImageUrl', response.data.signatureImageUrl || '');
      }
    } catch (error) {
      console.error('Error fetching certificate template:', error);
      toast.error('An error occurred while fetching the certificate template');
    } finally {
      setSubmitting(false);
    }
  };

  useEffect(() => {
    if (!isOpen) {
      reset(DefaultCertificateTemplateFormValues);
      // Clean up preview URLs
      if (backgroundImage.previewUrl) {
        URL.revokeObjectURL(backgroundImage.previewUrl);
      }
      if (logoImage.previewUrl) {
        URL.revokeObjectURL(logoImage.previewUrl);
      }
      if (signatureImage.previewUrl) {
        URL.revokeObjectURL(signatureImage.previewUrl);
      }
      setBackgroundImage({ url: '', selectedFile: null, previewUrl: null });
      setLogoImage({ url: '', selectedFile: null, previewUrl: null });
      setSignatureImage({ url: '', selectedFile: null, previewUrl: null });
      if (fileUploaderRef.current) {
        fileUploaderRef.current.clearFiles();
      }
      if (logoUploaderRef.current) {
        logoUploaderRef.current.clearFiles();
      }
      if (signatureUploaderRef.current) {
        signatureUploaderRef.current.clearFiles();
      }
    }
  }, [
    isOpen,
    reset,
    backgroundImage.previewUrl,
    logoImage.previewUrl,
    signatureImage.previewUrl,
  ]);

  // Cleanup preview URLs on unmount
  useEffect(() => {
    return () => {
      if (backgroundImage.previewUrl) {
        URL.revokeObjectURL(backgroundImage.previewUrl);
      }
      if (logoImage.previewUrl) {
        URL.revokeObjectURL(logoImage.previewUrl);
      }
      if (signatureImage.previewUrl) {
        URL.revokeObjectURL(signatureImage.previewUrl);
      }
    };
  }, [
    backgroundImage.previewUrl,
    logoImage.previewUrl,
    signatureImage.previewUrl,
  ]);

  const handleFileUpload = (files: FileList) => {
    if (files && files.length > 0) {
      const file = files[0];
      // Revoke previous preview URL if exists
      if (backgroundImage.previewUrl) {
        URL.revokeObjectURL(backgroundImage.previewUrl);
      }
      // Create preview URL for the new file
      const previewUrl = URL.createObjectURL(file);
      setBackgroundImage({
        url: '',
        selectedFile: file,
        previewUrl: previewUrl,
      });
      // Update form value to pass validation
      setValue('backgroundImageUrl', 'pending-upload');
    }
  };

  const handleLogoUpload = (files: FileList) => {
    if (files && files.length > 0) {
      const file = files[0];
      // Revoke previous preview URL if exists
      if (logoImage.previewUrl) {
        URL.revokeObjectURL(logoImage.previewUrl);
      }
      // Create preview URL for the new file
      const previewUrl = URL.createObjectURL(file);
      setLogoImage({
        url: '',
        selectedFile: file,
        previewUrl: previewUrl,
      });
      // Update form value to pass validation
      setValue('logoImageUrl', 'pending-upload');
    }
  };

  const handleSignatureUpload = (files: FileList) => {
    if (files && files.length > 0) {
      const file = files[0];
      // Revoke previous preview URL if exists
      if (signatureImage.previewUrl) {
        URL.revokeObjectURL(signatureImage.previewUrl);
      }
      // Create preview URL for the new file
      const previewUrl = URL.createObjectURL(file);
      setSignatureImage({
        url: '',
        selectedFile: file,
        previewUrl: previewUrl,
      });
      // Update form value to pass validation
      setValue('signatureImageUrl', 'pending-upload');
    }
  };

  const handleSubmitForm = async (fields: TCertificateTemplateFormFields) => {
    setSubmitting(true);

    try {
      let backgroundImageUrl = backgroundImage.url;
      let logoImageUrl = logoImage.url;
      let signatureImageUrl = signatureImage.url;

      // Upload background image if a new file was selected
      if (backgroundImage.selectedFile) {
        const { url, error } = await uploadFile(
          backgroundImage.selectedFile,
          FileType.CONTENT,
        );
        if (error) {
          toast.error(error);
          setSubmitting(false);
          return;
        }
        backgroundImageUrl = url;
      }

      // Upload logo image if a new file was selected
      if (logoImage.selectedFile) {
        const { url, error } = await uploadFile(
          logoImage.selectedFile,
          FileType.CONTENT,
        );
        if (error) {
          toast.error(error);
          setSubmitting(false);
          return;
        }
        logoImageUrl = url;
      }

      // Upload signature image if a new file was selected
      if (signatureImage.selectedFile) {
        const { url, error } = await uploadFile(
          signatureImage.selectedFile,
          FileType.CONTENT,
        );
        if (error) {
          toast.error(error);
          setSubmitting(false);
          return;
        }
        signatureImageUrl = url;
      }

      // Validate that background image URL is set
      if (!backgroundImageUrl) {
        toast.error('Please upload a background image');
        setSubmitting(false);
        return;
      }

      // Validate that logo image URL is set
      if (!logoImageUrl) {
        toast.error('Please upload a logo image');
        setSubmitting(false);
        return;
      }

      // Validate that signature image URL is set
      if (!signatureImageUrl) {
        toast.error('Please upload a signature image');
        setSubmitting(false);
        return;
      }

      const payload = {
        templateName: fields.templateName,
        certificateTitle: fields.certificateTitle,
        certificateType: fields.certificateType,
        acknowledgement: fields.acknowledgement,
        completionStatus: fields.completionStatus,
        completionTitle: fields.completionTitle,
        logoImageUrl: logoImageUrl,
        signatureImageUrl: signatureImageUrl,
        signerName: fields.signerName,
        signerDesignation: fields.signerDesignation,
        signatureIdentity: fields.signatureIdentity,
        backgroundImageUrl: backgroundImageUrl,
        dynamicFields: fields.dynamicFields,
        status: fields.status,
        isDefault: fields.isDefault,
        isTrialTemplate: fields.isTrialTemplate,
      };

      const response = id
        ? await apiClient.put(API_END_POINTS.CERTIFICATE_TEMPLATE_UPDATE(id), {
            data: payload,
          })
        : await apiClient.post(API_END_POINTS.CERTIFICATE_TEMPLATE_CREATE, {
            data: payload,
          });

      if (isSuccessResponse(response.statusCode)) {
        toast.success(
          response.message || 'Certificate template created successfully',
        );
        setIsOpen(false);
        onSubmit();
        reset(DefaultCertificateTemplateFormValues);
        // Clean up preview URLs
        if (backgroundImage.previewUrl) {
          URL.revokeObjectURL(backgroundImage.previewUrl);
        }
        if (logoImage.previewUrl) {
          URL.revokeObjectURL(logoImage.previewUrl);
        }
        if (signatureImage.previewUrl) {
          URL.revokeObjectURL(signatureImage.previewUrl);
        }
        setBackgroundImage({ url: '', selectedFile: null, previewUrl: null });
        setLogoImage({ url: '', selectedFile: null, previewUrl: null });
        setSignatureImage({ url: '', selectedFile: null, previewUrl: null });
        if (fileUploaderRef.current) {
          fileUploaderRef.current.clearFiles();
        }
        if (logoUploaderRef.current) {
          logoUploaderRef.current.clearFiles();
        }
        if (signatureUploaderRef.current) {
          signatureUploaderRef.current.clearFiles();
        }
      } else {
        toast.error(
          response.data.message || 'Failed to create certificate template',
        );
      }
    } catch (error) {
      console.error('Error creating certificate template:', error);
      toast.error('An error occurred while creating the certificate template');
    } finally {
      setSubmitting(false);
    }
  };

  const hasBackgroundImage =
    backgroundImage.url || backgroundImage.selectedFile;
  const hasLogoImage = logoImage.url || logoImage.selectedFile;
  const hasSignatureImage = signatureImage.url || signatureImage.selectedFile;

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="content-max-h-[90vh] content-w-3/4 content-overflow-y-auto content-text-white">
        <DialogHeader>
          <DialogTitle>
            {id
              ? 'Edit Certificate Template'
              : 'Create New Certificate Template'}
          </DialogTitle>
          <DialogDescription>
            {id
              ? 'Edit the details below to update the certificate template'
              : 'Fill in the details below to create a new certificate template'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(handleSubmitForm)}>
          <div className="content-space-y-6">
            {/* Template Name - Full Width */}
            <div className="content-space-y-2">
              <Controller
                name="templateName"
                control={control}
                render={({ field: { onChange, value } }) => (
                  <>
                    <Label
                      htmlFor="template-name"
                      className="content-text-white"
                    >
                      Template Name *
                    </Label>
                    <Input
                      id="template-name"
                      placeholder="Enter template name"
                      value={value}
                      onChange={e => onChange(e.target.value)}
                      className={
                        errors.templateName ? 'content-border-red-500' : ''
                      }
                    />
                    {errors.templateName && (
                      <p className="content-text-sm content-text-red-500">
                        {errors.templateName.message}
                      </p>
                    )}
                  </>
                )}
              />
            </div>

            {/* Grid Layout for Form Fields */}
            <div className="content-grid content-grid-cols-2 content-gap-4">
              <div className="content-space-y-2">
                <Controller
                  name="certificateTitle"
                  control={control}
                  render={({ field: { onChange, value } }) => (
                    <>
                      <Label
                        htmlFor="certificate-title"
                        className="content-text-white"
                      >
                        Certificate Title *
                      </Label>
                      <Input
                        id="certificate-title"
                        placeholder="Enter certificate title"
                        value={value}
                        onChange={e => onChange(e.target.value)}
                        className={
                          errors.certificateTitle
                            ? 'content-border-red-500'
                            : ''
                        }
                      />
                      {errors.certificateTitle && (
                        <p className="content-text-sm content-text-red-500">
                          {errors.certificateTitle.message}
                        </p>
                      )}
                    </>
                  )}
                />
              </div>

              <div className="content-space-y-2">
                <Controller
                  name="certificateType"
                  control={control}
                  render={({ field: { onChange, value } }) => (
                    <>
                      <Label
                        htmlFor="certificate-type"
                        className="content-text-white"
                      >
                        Certificate Type *
                      </Label>
                      <Input
                        id="certificate-type"
                        placeholder="Enter certificate type"
                        value={value}
                        onChange={e => onChange(e.target.value)}
                        className={
                          errors.certificateType ? 'content-border-red-500' : ''
                        }
                      />
                      {errors.certificateType && (
                        <p className="content-text-sm content-text-red-500">
                          {errors.certificateType.message}
                        </p>
                      )}
                    </>
                  )}
                />
              </div>

              <div className="content-space-y-2">
                <Controller
                  name="acknowledgement"
                  control={control}
                  render={({ field: { onChange, value } }) => (
                    <>
                      <Label
                        htmlFor="acknowledgement"
                        className="content-text-white"
                      >
                        Acknowledgement *
                      </Label>
                      <Input
                        id="acknowledgement"
                        placeholder="Enter acknowledgement"
                        value={value}
                        onChange={e => onChange(e.target.value)}
                        className={
                          errors.acknowledgement ? 'content-border-red-500' : ''
                        }
                      />
                      {errors.acknowledgement && (
                        <p className="content-text-sm content-text-red-500">
                          {errors.acknowledgement.message}
                        </p>
                      )}
                    </>
                  )}
                />
              </div>

              <div className="content-space-y-2">
                <Controller
                  name="completionStatus"
                  control={control}
                  render={({ field: { onChange, value } }) => (
                    <>
                      <Label
                        htmlFor="completion-status"
                        className="content-text-white"
                      >
                        Completion Status *
                      </Label>
                      <Input
                        id="completion-status"
                        placeholder="Enter completion status"
                        value={value}
                        onChange={e => onChange(e.target.value)}
                        className={
                          errors.completionStatus
                            ? 'content-border-red-500'
                            : ''
                        }
                      />
                      {errors.completionStatus && (
                        <p className="content-text-sm content-text-red-500">
                          {errors.completionStatus.message}
                        </p>
                      )}
                    </>
                  )}
                />
              </div>

              <div className="content-space-y-2">
                <Controller
                  name="completionTitle"
                  control={control}
                  render={({ field: { onChange, value } }) => (
                    <>
                      <Label
                        htmlFor="completion-title"
                        className="content-text-white"
                      >
                        Completion Title *
                      </Label>
                      <Input
                        id="completion-title"
                        placeholder="Enter completion title"
                        value={value}
                        onChange={e => onChange(e.target.value)}
                        className={
                          errors.completionTitle ? 'content-border-red-500' : ''
                        }
                      />
                      {errors.completionTitle && (
                        <p className="content-text-sm content-text-red-500">
                          {errors.completionTitle.message}
                        </p>
                      )}
                    </>
                  )}
                />
              </div>

              <div className="content-space-y-2">
                <Controller
                  name="signerName"
                  control={control}
                  render={({ field: { onChange, value } }) => (
                    <>
                      <Label
                        htmlFor="signer-name"
                        className="content-text-white"
                      >
                        Signer Name *
                      </Label>
                      <Input
                        id="signer-name"
                        placeholder="Enter signer name"
                        value={value}
                        onChange={e => onChange(e.target.value)}
                        className={
                          errors.signerName ? 'content-border-red-500' : ''
                        }
                      />
                      {errors.signerName && (
                        <p className="content-text-sm content-text-red-500">
                          {errors.signerName.message}
                        </p>
                      )}
                    </>
                  )}
                />
              </div>

              <div className="content-space-y-2">
                <Controller
                  name="signerDesignation"
                  control={control}
                  render={({ field: { onChange, value } }) => (
                    <>
                      <Label
                        htmlFor="signer-designation"
                        className="content-text-white"
                      >
                        Signer Designation *
                      </Label>
                      <Input
                        id="signer-designation"
                        placeholder="Enter signer designation"
                        value={value}
                        onChange={e => onChange(e.target.value)}
                        className={
                          errors.signerDesignation
                            ? 'content-border-red-500'
                            : ''
                        }
                      />
                      {errors.signerDesignation && (
                        <p className="content-text-sm content-text-red-500">
                          {errors.signerDesignation.message}
                        </p>
                      )}
                    </>
                  )}
                />
              </div>

              <div className="content-space-y-2">
                <Controller
                  name="signatureIdentity"
                  control={control}
                  render={({ field: { onChange, value } }) => (
                    <>
                      <Label
                        htmlFor="signature-identity"
                        className="content-text-white"
                      >
                        Signature Identity
                      </Label>
                      <Input
                        id="signature-identity"
                        placeholder="Enter signature identity"
                        value={value}
                        onChange={e => onChange(e.target.value)}
                        className={
                          errors.signatureIdentity
                            ? 'content-border-red-500'
                            : ''
                        }
                      />
                      {errors.signatureIdentity && (
                        <p className="content-text-sm content-text-red-500">
                          {errors.signatureIdentity.message}
                        </p>
                      )}
                    </>
                  )}
                />
              </div>
            </div>

            <div>
              <Label htmlFor="logo-upload" className="content-text-white">
                Upload Logo Image *
              </Label>
              <div className="content-flex content-items-center content-justify-center">
                <FileUploader
                  ref={logoUploaderRef}
                  id="logo-upload"
                  accept="image/png,image/jpeg,image/jpg"
                  maxSize={10}
                  variant="secondary"
                  onUpload={handleLogoUpload}
                  placeholder={
                    hasLogoImage ? (
                      <div className="content-flex content-flex-col content-items-center content-justify-center content-gap-3 content-text-cloudy-white">
                        <Upload className="content-size-10 content-text-primary" />
                        REPLACE FILE
                        <p className="content-text-xs content-text-cloudy-white">
                          Allowable file types: PNG, JPEG, JPG
                        </p>
                      </div>
                    ) : (
                      <div className="content-flex content-flex-col content-items-center content-justify-center content-gap-3 content-text-cloudy-white">
                        <Upload className="content-size-10 content-text-primary" />
                        UPLOAD FILE
                        <p className="content-text-xs content-text-cloudy-white">
                          Allowable file types: PNG, JPEG, JPG
                        </p>
                      </div>
                    )
                  }
                  containerClassName="content-w-full content-py-4"
                />
              </div>
              {errors.logoImageUrl && (
                <p className="content-text-sm content-text-red-500">
                  {errors.logoImageUrl.message}
                </p>
              )}
              {/* Image Preview */}
              {(logoImage.previewUrl || logoImage.url) && (
                <div className="content-mt-4 content-space-y-2">
                  <Label className="content-text-sm content-text-cloudy-white">
                    Logo Preview:
                  </Label>
                  <div className="content-relative content-w-full content-overflow-hidden content-rounded-lg content-border content-border-card-border content-bg-gray-900">
                    <img
                      src={
                        logoImage.previewUrl || FILE_PATH_PREFIX + logoImage.url
                      }
                      alt="Logo Preview"
                      className="content-h-auto content-max-h-96 content-w-full content-object-contain"
                    />
                  </div>
                  {logoImage.selectedFile && (
                    <p className="content-text-xs content-text-gray-400">
                      File: {logoImage.selectedFile.name} (
                      {(logoImage.selectedFile.size / 1024 / 1024).toFixed(2)}{' '}
                      MB)
                    </p>
                  )}
                </div>
              )}
            </div>

            <div>
              <Label htmlFor="signature-upload" className="content-text-white">
                Upload Signature Image *
              </Label>
              <div className="content-flex content-items-center content-justify-center">
                <FileUploader
                  ref={signatureUploaderRef}
                  id="signature-upload"
                  accept="image/png,image/jpeg,image/jpg"
                  maxSize={10}
                  variant="secondary"
                  onUpload={handleSignatureUpload}
                  placeholder={
                    hasSignatureImage ? (
                      <div className="content-flex content-flex-col content-items-center content-justify-center content-gap-3 content-text-cloudy-white">
                        <Upload className="content-size-10 content-text-primary" />
                        REPLACE FILE
                        <p className="content-text-xs content-text-cloudy-white">
                          Allowable file types: PNG, JPEG, JPG
                        </p>
                      </div>
                    ) : (
                      <div className="content-flex content-flex-col content-items-center content-justify-center content-gap-3 content-text-cloudy-white">
                        <Upload className="content-size-10 content-text-primary" />
                        UPLOAD FILE
                        <p className="content-text-xs content-text-cloudy-white">
                          Allowable file types: PNG, JPEG, JPG
                        </p>
                      </div>
                    )
                  }
                  containerClassName="content-w-full content-py-4"
                />
              </div>
              {errors.signatureImageUrl && (
                <p className="content-text-sm content-text-red-500">
                  {errors.signatureImageUrl.message}
                </p>
              )}
              {/* Image Preview */}
              {(signatureImage.previewUrl || signatureImage.url) && (
                <div className="content-mt-4 content-space-y-2">
                  <Label className="content-text-sm content-text-cloudy-white">
                    Signature Preview:
                  </Label>
                  <div className="content-relative content-w-full content-overflow-hidden content-rounded-lg content-border content-border-card-border content-bg-gray-900">
                    <img
                      src={
                        signatureImage.previewUrl ||
                        FILE_PATH_PREFIX + signatureImage.url
                      }
                      alt="Signature Preview"
                      className="content-h-auto content-max-h-96 content-w-full content-object-contain"
                    />
                  </div>
                  {signatureImage.selectedFile && (
                    <p className="content-text-xs content-text-gray-400">
                      File: {signatureImage.selectedFile.name} (
                      {(signatureImage.selectedFile.size / 1024 / 1024).toFixed(
                        2,
                      )}{' '}
                      MB)
                    </p>
                  )}
                </div>
              )}
            </div>
            {id && (
              <div className="content-space-y-2">
                <Label className="content-text-white">Status:</Label>
                <Controller
                  name="status"
                  control={control}
                  render={({ field: { onChange, value } }) => (
                    <Select
                      value={value}
                      onValueChange={value => onChange(value)}
                    >
                      <SelectTrigger
                        className={
                          errors.status ? 'content-border-red-500' : ''
                        }
                      >
                        <SelectValue placeholder="Select status" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value={Status.ENABLED}>Enabled</SelectItem>
                        <SelectItem value={Status.DISABLED}>
                          Disabled
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
            )}

            <div>
              <Label htmlFor="background-upload" className="content-text-white">
                Upload Background Image *
              </Label>
              <div className="content-flex content-items-center content-justify-center">
                <FileUploader
                  ref={fileUploaderRef}
                  id="background-upload"
                  accept="image/png,image/jpeg,image/jpg"
                  maxSize={10}
                  variant="secondary"
                  onUpload={handleFileUpload}
                  placeholder={
                    hasBackgroundImage ? (
                      <div className="content-flex content-flex-col content-items-center content-justify-center content-gap-3 content-text-cloudy-white">
                        <Upload className="content-size-10 content-text-primary" />
                        REPLACE FILE
                        <p className="content-text-xs content-text-cloudy-white">
                          Allowable file types: PNG, JPEG, JPG
                        </p>
                      </div>
                    ) : (
                      <div className="content-flex content-flex-col content-items-center content-justify-center content-gap-3 content-text-cloudy-white">
                        <Upload className="content-size-10 content-text-primary" />
                        UPLOAD FILE
                        <p className="content-text-xs content-text-cloudy-white">
                          Allowable file types: PNG, JPEG, JPG
                        </p>
                      </div>
                    )
                  }
                  containerClassName="content-w-full content-py-4"
                />
              </div>
              {errors.backgroundImageUrl && (
                <p className="content-text-sm content-text-red-500">
                  {errors.backgroundImageUrl.message}
                </p>
              )}
              {/* Image Preview */}
              {(backgroundImage.previewUrl || backgroundImage.url) && (
                <div className="content-mt-4 content-space-y-2">
                  <Label className="content-text-sm content-text-cloudy-white">
                    Preview:
                  </Label>
                  <div className="content-relative content-w-full content-overflow-hidden content-rounded-lg content-border content-border-card-border content-bg-gray-900">
                    <img
                      src={
                        backgroundImage.previewUrl ||
                        FILE_PATH_PREFIX + backgroundImage.url
                      }
                      alt="Preview"
                      className="content-h-auto content-max-h-96 content-w-full content-object-contain"
                    />
                  </div>
                  {backgroundImage.selectedFile && (
                    <p className="content-text-xs content-text-gray-400">
                      File: {backgroundImage.selectedFile.name} (
                      {(
                        backgroundImage.selectedFile.size /
                        1024 /
                        1024
                      ).toFixed(2)}{' '}
                      MB)
                    </p>
                  )}
                </div>
              )}
            </div>

            <div className="content-space-y-2">
              <Label htmlFor="dynamic-fields" className="content-text-white">
                Dynamic Fields
              </Label>
              <div className="content-grid content-grid-cols-2 content-gap-2">
                {DYNAMIC_FIELDS.map(field => (
                  <Label
                    htmlFor={field.key}
                    key={field.key}
                    className="content-flex content-w-fit content-cursor-pointer content-items-center content-gap-2"
                  >
                    <Controller
                      name={`dynamicFields.${field.key}`}
                      control={control}
                      render={({ field: { onChange, value } }) => (
                        <Checkbox
                          id={field.key}
                          checked={value}
                          onCheckedChange={checked => onChange(checked)}
                        />
                      )}
                    />
                    {field.label}
                  </Label>
                ))}
              </div>
            </div>
            <div className="content-mt-4 content-grid content-grid-cols-2 content-gap-x-5">
              <div className="content-space-y-2">
                <Label className="content-flex content-items-center content-gap-2 content-text-white">
                  Set as Default Template{' '}
                  <span title='If you check "Set as Default Template", this template will be automatically generated for the client by default.'>
                    {' '}
                    <InfoIcon className="content-size-4 content-text-primary" />
                  </span>
                </Label>
                <Controller
                  name="isDefault"
                  control={control}
                  render={({ field: { onChange, value } }) => (
                    <Label
                      htmlFor="isDefault"
                      className="content-flex content-cursor-pointer content-items-center content-gap-2 content-rounded-lg content-bg-primary/10 content-px-4 content-py-2"
                    >
                      <Checkbox
                        id="isDefault"
                        checked={Boolean(value)}
                        onCheckedChange={checked => onChange(checked)}
                      />
                      Set as Default Template
                    </Label>
                  )}
                />
              </div>

              <div className="content-space-y-2">
                <Label className="content-flex content-items-center content-gap-2 content-text-white">
                  Set as Trial Template{' '}
                  <span title='If you check "Set as Trial Template", this template will be available for the trial clients.'>
                    {' '}
                    <InfoIcon className="content-size-4 content-text-primary" />
                  </span>
                </Label>
                <Controller
                  name="isTrialTemplate"
                  control={control}
                  render={({ field: { onChange, value } }) => (
                    <Label
                      htmlFor="isTrialTemplate"
                      className="content-flex content-cursor-pointer content-items-center content-gap-2 content-rounded-lg content-bg-primary/10 content-px-4 content-py-2"
                    >
                      <Checkbox
                        id="isTrialTemplate"
                        checked={Boolean(value)}
                        onCheckedChange={checked => onChange(checked)}
                      />
                      Set as Trial Template
                    </Label>
                  )}
                />
              </div>
            </div>

            <div className="content-flex content-justify-end content-gap-2">
              <Button
                variant="outline"
                type="button"
                onClick={() => setIsOpen(false)}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={submitting}>
                {submitting ? 'Creating...' : 'Create'}
              </Button>
            </div>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default TemplateCreateDialog;
