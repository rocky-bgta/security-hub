import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectValue,
  SelectItem,
  SelectTrigger,
  SelectContent,
} from 'common/Select';
import CustomSelect from 'common/CustomSelect';
import AIGenerateModal from 'features/email-template/AIGenerateModal';
import EmailTemplateEditor from 'features/email-template/EmailTemplateEditor';
import SmsDeliveryRulesCard from 'features/email-template/SmsDeliveryRulesCard';
import SmsTemplateEditor, {
  type SmsDeliveryMode,
} from 'features/email-template/SmsTemplateEditor';
import EmployeeDataFieldSelector from 'features/email-template/EmployeeDataFieldSelector';
import TagSelector from 'features/email-template/TagSelector';
import TemplatePreview from 'features/email-template/TemplatePreview';
import EmailTemplateFieldsHelpModal from 'features/email-template/EmailTemplateFieldsHelpModal';
import useEmailTemplates from 'hooks/UseEmailTemplates';
import { useLandingPages } from 'hooks/UseLandingPages';
import { useUploader } from 'hooks/UseUploader';
import {
  HelpCircle,
  Loader2,
  MessageSquare,
  Mail,
  Save,
  Sparkles,
  Trash,
  Upload,
} from 'lucide-react';
import {
  EmployeeDataField,
  TemplateType,
  toIdName,
} from 'models/EmailTemplate';
import { CampaignChannel } from 'models/Campaign';
import { IDomainListResponse } from 'models/Domain';
import { FileType, IResponse } from 'models/Global';
import { ICustomSelectOption, TMultiValue } from 'models/Input';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useCallback } from 'react';
import { Controller, useForm, type Resolver } from 'react-hook-form';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getTemplateLibraryUrl } from 'utils/ListNavigation';
import {
  DefaultEmailTemplateCreateValues,
  DefaultEmailTemplateUpdateValues,
  EmailTemplateCreateSchema,
  EmailTemplateUpdateSchema,
  TAIGenerateForm,
  TEmailTemplateCreateForm,
  TEmailTemplateUpdateForm,
} from 'schemas/EmailTemplateSchema';
import { cn } from 'utils/Helper';
import {
  buildSmsPhishingPreviewUrl,
  createSmsPhishingPathSegment,
  getSmsBodyFromPreview,
  getSmsEffectiveLength,
  PHISHING_LINK_TOKEN,
  SMS_MAX_CHARACTERS,
  truncateSmsBodyToEffectiveLimit,
} from 'utils/smsSegments';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { IEmailTemplateFixHtmlRequest } from 'models/EmailTemplate';
import SearchSelect from 'components/SearchSelect';
import IconBackButton from 'components/IconBackButton';
import useDropDown from 'hooks/UseDropDown';
import { useAPI } from 'hooks/UseAPI';
import { IDropdownItem } from 'models/DropDown';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface EmailTemplateCreateProps {
  forcedTemplateType?: TemplateType;
}

const templateTypeToChannel = (templateType: TemplateType): CampaignChannel =>
  templateType === TemplateType.SMS
    ? CampaignChannel.SMS
    : CampaignChannel.EMAIL;

function toSmsApiPayload<
  T extends {
    emailBody: string;
    emailBodyText?: string;
    allowMultiPartSms?: boolean;
  },
>(
  data: T,
): Omit<T, 'emailBody' | 'emailBodyText' | 'allowMultiPartSms'> & {
  smsBody: string;
} {
  const {
    emailBody,
    emailBodyText: _text,
    allowMultiPartSms: _allowMultiPartSms,
    ...rest
  } = data;
  return { ...rest, smsBody: emailBody };
}

/**
 * Create / Edit Template page (Email & SMS).
 * Pass `forcedTemplateType` from dedicated SMS/email wrappers.
 */
const EmailTemplateCreate = ({
  forcedTemplateType = TemplateType.EMAIL,
}: EmailTemplateCreateProps) => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditMode = !!id;
  const initialTemplateType = forcedTemplateType;
  const MAX_UPLOAD_SIZE_BYTES = 2 * 1024 * 1024; // 2MB

  const { uploadFile } = useUploader();
  const apiClient = useAPI();
  const {
    createTemplate,
    updateTemplate,
    getTemplateDetails,
    getTemplatePreview,
    generateAITemplate,
    fixEmailTemplateHtml,
    creating,
  } = useEmailTemplates();
  const {
    fetchLandingPages,
    loading: landingPagesLoading,
    landingPages,
  } = useLandingPages();
  const { fetchPayloadTypes, fetchLanguages, fetchDifficulties } =
    useDropDown();
  const [showAIModal, setShowAIModal] = useState(false);
  const [showFieldsHelpModal, setShowFieldsHelpModal] = useState(false);

  const [loadingExisting, setLoadingExisting] = useState(false);
  const [thumbnailFile, setThumbnailFile] = useState<File | null>(null);
  const [thumbnailPreviewUrl, setThumbnailPreviewUrl] = useState<string | null>(
    null,
  );
  const [payloadTypes, setPayloadTypes] = useState<IDropdownItem[]>([]);
  const [languages, setLanguages] = useState<
    { id: string; displayName: string }[]
  >([]);
  const [difficultiesList, setDifficultiesList] = useState<IDropdownItem[]>([]);
  const [attachment, setAttachment] = useState<{
    links: string[];
    files: File[];
  }>({
    links: [],
    files: [],
  });
  const MAX_ATTACHMENTS = 5;
  const [smsDeliveryMode, setSmsDeliveryMode] =
    useState<SmsDeliveryMode>('single');
  const [trackingDomain, setTrackingDomain] = useState('');
  const [domainById, setDomainById] = useState<Record<string, string>>({});
  const smsPhishingPathSegmentRef = useRef(createSmsPhishingPathSegment());

  const landingPageOptions = useMemo<ICustomSelectOption[]>(
    () =>
      landingPages.items.map(page => ({
        id: page.pageId,
        label: page.name,
        value: page.pageId,
        trackingDomain: page.trackingDomainId,
      })),
    [landingPages.items],
  );

  const {
    register,
    control,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors, isValid, isSubmitting },
  } = useForm<TEmailTemplateCreateForm | TEmailTemplateUpdateForm>({
    resolver: zodResolver(
      isEditMode ? EmailTemplateUpdateSchema : EmailTemplateCreateSchema,
    ) as Resolver<TEmailTemplateCreateForm | TEmailTemplateUpdateForm>,
    defaultValues: isEditMode
      ? DefaultEmailTemplateUpdateValues
      : {
          ...DefaultEmailTemplateCreateValues,
          templateType: initialTemplateType,
        },
    mode: 'onChange',
  });

  useEffect(() => {
    const loadPayloadTypes = async () => {
      const data = await fetchPayloadTypes(
        templateTypeToChannel(initialTemplateType),
      );
      setPayloadTypes(data.items);
      if (isEditMode) return;
      const defaultItem =
        data.items.find(item => item.isDefault) ?? data.items[0];
      if (defaultItem) {
        setValue(
          'payloadType',
          { id: defaultItem.id, name: defaultItem.name },
          { shouldValidate: true, shouldDirty: true },
        );
      }
    };
    loadPayloadTypes();
  }, [fetchPayloadTypes, initialTemplateType, isEditMode, setValue]);

  useEffect(() => {
    const loadLanguages = async () => {
      const data = await fetchLanguages();
      setLanguages(data.data ?? []);
      if (isEditMode) return;
      setValue(
        'language',
        data.data?.find(item => item.displayName === 'English')?.displayName ??
          '',
        { shouldValidate: true, shouldDirty: true },
      );
    };
    loadLanguages();
  }, [fetchLanguages, setValue, isEditMode]);

  useEffect(() => {
    const loadDifficultiesList = async () => {
      const data = await fetchDifficulties();
      setDifficultiesList(data.items);
      if (isEditMode) return;
      const defaultItem =
        data.items.find(item => item.isDefault) ?? data.items[0];
      if (defaultItem) {
        setValue(
          'difficultyLevel',
          { id: defaultItem.id, name: defaultItem.name },
          { shouldValidate: true, shouldDirty: true },
        );
      }
    };
    loadDifficultiesList();
  }, [fetchDifficulties, isEditMode, setValue]);

  useEffect(() => {
    fetchLandingPages({
      offset: 0,
      pageSize: 1000,
      status: 'ACTIVE',
      sortBy: 'name',
      sortOrder: 'asc',
    });
  }, [fetchLandingPages]);

  // Load existing template in edit mode
  useEffect(() => {
    const loadExisting = async () => {
      if (!isEditMode || !id) return;
      setLoadingExisting(true);
      try {
        const details = await getTemplateDetails(id);
        if (!details) return;

        const [preview, payloadData, difficultyData] = await Promise.all([
          getTemplatePreview(id),
          fetchPayloadTypes(templateTypeToChannel(forcedTemplateType)),
          fetchDifficulties(),
        ]);

        setPayloadTypes(payloadData.items);
        setDifficultiesList(difficultyData.items);

        const isSms =
          forcedTemplateType === TemplateType.SMS ||
          details.templateType === TemplateType.SMS;
        const bodyFromPreview = isSms
          ? getSmsBodyFromPreview(preview)
          : (preview?.emailBody ?? '');
        const initialSmsDeliveryMode: SmsDeliveryMode =
          bodyFromPreview.length > SMS_MAX_CHARACTERS ? 'multi' : 'single';

        setSmsDeliveryMode(initialSmsDeliveryMode);

        reset({
          ...DefaultEmailTemplateUpdateValues,
          templateType: forcedTemplateType,
          templateName: details.templateName ?? '',
          description: details.description ?? '',
          status:
            (details as unknown as { status?: unknown }).status === 'ACTIVE' ||
            (details as unknown as { status?: unknown }).status ===
              'INACTIVE' ||
            (details as unknown as { status?: unknown }).status === 'DRAFT'
              ? ((
                  details as unknown as {
                    status?: 'ACTIVE' | 'INACTIVE' | 'DRAFT';
                  }
                ).status ?? 'DRAFT')
              : 'DRAFT',
          emailType: details.emailType,
          payloadType: toIdName(details.payloadType, payloadData.items),
          emailSubject: details.emailSubject ?? '',
          emailBody: bodyFromPreview,
          emailBodyText: isSms ? '' : (preview?.emailBodyText ?? ''),
          difficultyLevel: toIdName(
            details.difficultyLevel,
            difficultyData.items,
          ),
          serviceLocation: details.serviceLocation ?? '',
          tags: details.tags ?? [],
          employeeDataRequired: details.employeeDataRequired ?? [],
          thumbnailUrl: details.thumbnailUrl ?? '',
          language: details.language ?? '',
          attachments: details.attachments ?? [],
          landingPageIds: details.landingPageIds ?? [],
          allowMultiPartSms: initialSmsDeliveryMode === 'multi',
        });

        setAttachment({
          links: details.attachments,
          files: [],
        });
        setThumbnailFile(null);
      } finally {
        setLoadingExisting(false);
      }
    };

    loadExisting();
  }, [
    fetchDifficulties,
    fetchPayloadTypes,
    forcedTemplateType,
    getTemplateDetails,
    getTemplatePreview,
    id,
    isEditMode,
    reset,
  ]);

  // Watch values for preview
  const templateName = watch('templateName');
  const emailSubject = watch('emailSubject');
  const emailBody = watch('emailBody');
  const emailType = watch('emailType');
  const thumbnailUrl = watch('thumbnailUrl');
  const watchedLandingPageIds = watch('landingPageIds');
  const landingPageIds = useMemo(
    () => watchedLandingPageIds ?? [],
    [watchedLandingPageIds],
  );

  const templateType = watch('templateType') ?? TemplateType.EMAIL;
  const isSmsTemplate = templateType === TemplateType.SMS;
  const statusValue = watch('status');

  // Fetch verified domains for SMS tracking-domain resolution
  useEffect(() => {
    if (!isSmsTemplate) return;

    const loadDomains = async () => {
      try {
        const response = (await apiClient.get(
          `${API_END_POINTS.DOMAIN_LIST}offset=0&pageSize=1000&status=VERIFIED&status=VERIFIED_AND_LOCKED`,
        )) as IResponse<IDomainListResponse>;
        if (response?.data?.items) {
          const map: Record<string, string> = {};
          for (const domain of response.data.items) {
            map[domain.domainId] = domain.domain;
          }
          setDomainById(map);
        }
      } catch (error) {
        console.error('Error fetching domains:', error);
      }
    };

    loadDomains();
  }, [apiClient, isSmsTemplate]);

  // Store tracking domain from the first selected landing page
  useEffect(() => {
    if (!isSmsTemplate) {
      setTrackingDomain('');
      return;
    }

    const firstLandingPageId = landingPageIds[0];
    if (!firstLandingPageId) {
      setTrackingDomain('');
      return;
    }

    const page = landingPages.items.find(p => p.pageId === firstLandingPageId);
    const domainId = page?.trackingDomainId;
    setTrackingDomain(domainId ? (domainById[domainId] ?? '') : '');
  }, [domainById, isSmsTemplate, landingPageIds, landingPages.items]);

  const phishingPreviewUrl = useMemo(() => {
    if (!isSmsTemplate || !trackingDomain) return undefined;
    return buildSmsPhishingPreviewUrl(
      trackingDomain,
      smsPhishingPathSegmentRef.current,
    );
  }, [isSmsTemplate, trackingDomain]);

  const handleSmsDeliveryModeChange = useCallback(
    (mode: SmsDeliveryMode) => {
      if (
        mode === 'single' &&
        getSmsEffectiveLength(emailBody ?? '', phishingPreviewUrl) >
          SMS_MAX_CHARACTERS
      ) {
        toast.error(
          `Shorten your message to ${SMS_MAX_CHARACTERS} characters or fewer (including the phishing link URL) before switching to Single SMS.`,
        );
        return;
      }
      setSmsDeliveryMode(mode);
      setValue('allowMultiPartSms', mode === 'multi', {
        shouldValidate: true,
        shouldDirty: true,
      });
    },
    [emailBody, phishingPreviewUrl, setValue],
  );

  // Keep single-SMS body within the URL-aware limit when tracking domain resolves
  useEffect(() => {
    if (!isSmsTemplate || smsDeliveryMode !== 'single' || !phishingPreviewUrl) {
      return;
    }
    if (!emailBody?.includes(PHISHING_LINK_TOKEN)) return;
    if (
      getSmsEffectiveLength(emailBody, phishingPreviewUrl) <= SMS_MAX_CHARACTERS
    ) {
      return;
    }

    const truncated = truncateSmsBodyToEffectiveLimit(
      emailBody,
      phishingPreviewUrl,
    );
    if (truncated !== emailBody) {
      setValue('emailBody', truncated, {
        shouldValidate: true,
        shouldDirty: true,
      });
    }
  }, [emailBody, isSmsTemplate, phishingPreviewUrl, setValue, smsDeliveryMode]);

  // Thumbnail preview: create/revoke object URL when thumbnailFile changes
  useEffect(() => {
    if (!thumbnailFile) {
      setThumbnailPreviewUrl(prev => {
        if (prev) URL.revokeObjectURL(prev);
        return null;
      });
      return;
    }
    const url = URL.createObjectURL(thumbnailFile);
    setThumbnailPreviewUrl(prev => {
      if (prev) URL.revokeObjectURL(prev);
      return url;
    });
    return () => URL.revokeObjectURL(url);
  }, [thumbnailFile]);

  const navigateToLibrary = () => {
    navigate(getTemplateLibraryUrl(templateType));
  };

  /** Store thumbnail file from device; upload happens on submit */
  const handleThumbnailFileSelect = (file: File) => {
    if (file.size > MAX_UPLOAD_SIZE_BYTES) {
      toast.error('Thumbnail must be 2MB or smaller.');
      return;
    }
    setThumbnailFile(file);
    setValue('thumbnailUrl', '', { shouldValidate: true, shouldDirty: true });
  };

  const clearThumbnail = () => {
    setThumbnailFile(null);
    setValue('thumbnailUrl', '', { shouldValidate: true, shouldDirty: true });
  };

  // Handle form submission: upload thumbnail + attachments to backend, then create/update
  const handleFormSubmit = async (
    data: TEmailTemplateCreateForm | TEmailTemplateUpdateForm,
  ) => {
    let thumbnailUrlFinal = '';
    if (typeof data.thumbnailUrl === 'string') {
      thumbnailUrlFinal = data.thumbnailUrl;
    }

    // 1. Upload thumbnail from device to backend (if user selected a file)
    if (thumbnailFile) {
      const { url, error } = await uploadFile(
        thumbnailFile,
        FileType.THUMBNAIL,
      );
      if (error || !url) {
        toast.error(error || 'Failed to upload thumbnail. Please try again.');
        return;
      }
      thumbnailUrlFinal = url;
    }

    if (isEditMode && id) {
      // 2a. Edit: upload any pending attachment files to backend
      const attachmentUrls: string[] = [];
      if (!isSmsTemplate) {
        const pendingFiles = attachment.files;
        for (const file of pendingFiles) {
          const { url, error } = await uploadFile(file, FileType.CONTENT);
          if (error || !url) {
            toast.error(
              error ||
                `Failed to upload attachment: ${file.name}. Please try again.`,
            );
            return;
          }
          attachmentUrls.push(url);
        }
      }

      const baseUpdate = isSmsTemplate
        ? toSmsApiPayload(data as TEmailTemplateUpdateForm)
        : (data as TEmailTemplateUpdateForm);

      const result = await updateTemplate(id, {
        ...baseUpdate,
        thumbnailUrl: thumbnailUrlFinal,
        attachments: isSmsTemplate
          ? []
          : [...attachment.links, ...attachmentUrls],
      });
      if (result) {
        navigateToLibrary();
      }
      return;
    }

    // 2b. Create: upload all pending attachment files to backend
    const createData = data as TEmailTemplateCreateForm;
    const attachmentUrls: string[] = [];
    if (!isSmsTemplate) {
      const pendingFiles = attachment.files;
      for (const file of pendingFiles) {
        const { url, error } = await uploadFile(file, FileType.CONTENT);
        if (error || !url) {
          toast.error(
            error ||
              `Failed to upload attachment: ${file.name}. Please try again.`,
          );
          return;
        }
        attachmentUrls.push(url);
      }
    }

    const baseCreate = isSmsTemplate ? toSmsApiPayload(createData) : createData;

    const result = await createTemplate({
      ...baseCreate,
      thumbnailUrl: thumbnailUrlFinal,
      attachments: isSmsTemplate ? [] : attachmentUrls,
    });
    if (result) {
      navigateToLibrary();
    }
  };

  // Handle AI generation
  const handleAIGenerate = async (data: TAIGenerateForm) => {
    const result = await generateAITemplate(data);
    if (result) {
      setShowAIModal(false);
      navigateToLibrary();
    }
  };

  // Store attachment file locally; upload to backend only on submit
  const handleUploadAttachment = async (file: File): Promise<File> => {
    if (file.size > MAX_UPLOAD_SIZE_BYTES) {
      toast.error('Attachment must be 2MB or smaller.');
      return file;
    }
    setAttachment(prev => {
      const total = prev.links.length + prev.files.length;
      if (total >= MAX_ATTACHMENTS) {
        toast.error('Maximum 5 attachments allowed.');
        return prev;
      }

      const exists = prev.files.some(existing => existing.name === file.name);
      if (exists) {
        toast.info('Attachment already selected.');
        return prev;
      }

      return { ...prev, files: [...prev.files, file] };
    });
    return file;
  };

  const handleDeletePendingAttachment = (fileName: string): void => {
    setAttachment(prev => ({
      ...prev,
      files: prev.files.filter(a => a.name !== fileName),
    }));
  };

  const handleDeleteExistingAttachment = (link: string): void => {
    setAttachment(prev => ({
      ...prev,
      links: prev.links.filter(existing => existing !== link),
    }));
  };

  const isSaving = creating || isSubmitting;
  const isBusy = isSaving || loadingExisting;

  const handleEditorAIFix = useCallback(
    async (params: IEmailTemplateFixHtmlRequest) => {
      const result = await fixEmailTemplateHtml(params);
      return result ?? undefined;
    },
    [fixEmailTemplateHtml],
  );

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="space-y-3">
        <IconBackButton
          onClick={navigateToLibrary}
          label={
            isSmsTemplate ? 'Back to SMS Templates' : 'Back to Email Templates'
          }
        />
        <div className="flex items-center justify-between">
          <div>
            <h1 className="flex items-center gap-2 text-2xl font-semibold text-white">
              {isSmsTemplate ? (
                <MessageSquare className="size-7 text-primary" />
              ) : (
                <Mail className="size-7 text-primary" />
              )}
              {isEditMode
                ? isSmsTemplate
                  ? 'Edit SMS Template'
                  : 'Edit Email Template'
                : isSmsTemplate
                  ? 'Create SMS Template'
                  : 'Create Email Template'}
            </h1>
            <p className="mt-1 text-muted-foreground">
              {isEditMode
                ? isSmsTemplate
                  ? 'Update your phishing SMS template'
                  : 'Update your phishing email template'
                : isSmsTemplate
                  ? 'Create a new SMS template for smishing simulations'
                  : 'Create a new phishing email template for your campaigns'}
            </p>
          </div>
          <div className="flex items-center gap-3">
            {!isEditMode && !isSmsTemplate && (
              <Button variant="outline" onClick={() => setShowAIModal(true)}>
                <Sparkles className="mr-2 size-4" />
                Create with AI
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="grid gap-6 lg:grid-cols-3">
        {/* Form Section */}
        <div className="lg:col-span-2">
          <form onSubmit={handleSubmit(handleFormSubmit)}>
            <Card>
              <CardHeader>
                <CardTitle>Template Details</CardTitle>
                <CardDescription>
                  Fill in the required information to create your{' '}
                  {isSmsTemplate ? 'SMS' : 'email'} template
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <input type="hidden" {...register('templateType')} />
                {/* Basic Info */}
                <div className="grid gap-4 md:grid-cols-3">
                  <div>
                    <Label htmlFor="templateName">
                      Template Name <span className="text-vibrant-red">*</span>
                    </Label>
                    <Input
                      id="templateName"
                      {...register('templateName')}
                      placeholder="Enter template name"
                      className={cn(
                        errors.templateName && 'border-vibrant-red',
                      )}
                      maxLength={100}
                    />
                    {errors.templateName && (
                      <p className="mt-1 text-sm text-vibrant-red">
                        {errors.templateName.message}
                      </p>
                    )}
                  </div>
                  <div>
                    <Label htmlFor="emailSubject">
                      {isSmsTemplate ? 'Message Preview' : 'Email Subject'}{' '}
                      <span className="text-vibrant-red">*</span>
                    </Label>
                    <Input
                      id="emailSubject"
                      {...register('emailSubject')}
                      placeholder={
                        isSmsTemplate
                          ? 'Enter SMS preview line'
                          : 'Enter email subject line'
                      }
                      className={cn(
                        errors.emailSubject && 'border-vibrant-red',
                      )}
                      maxLength={200}
                    />
                    {errors.emailSubject && (
                      <p className="mt-1 text-sm text-vibrant-red">
                        {errors.emailSubject.message}
                      </p>
                    )}
                  </div>
                  <div>
                    <Label htmlFor="status">
                      Status <span className="text-vibrant-red">*</span>
                    </Label>
                    <Select
                      value={statusValue}
                      onValueChange={value =>
                        setValue(
                          'status',
                          value as TEmailTemplateCreateForm['status'],
                          { shouldValidate: true },
                        )
                      }
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select status" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ACTIVE">Active</SelectItem>
                        <SelectItem value="INACTIVE">Inactive</SelectItem>
                        <SelectItem value="DRAFT">Draft</SelectItem>
                      </SelectContent>
                    </Select>
                    {errors.status && (
                      <p className="mt-1 text-sm text-vibrant-red">
                        {errors.status.message as string}
                      </p>
                    )}
                  </div>
                </div>

                {/* Description */}
                <div>
                  <Label htmlFor="description">Description</Label>
                  <textarea
                    id="description"
                    {...register('description')}
                    placeholder="Brief description of the template..."
                    className={cn(
                      'mt-1 w-full rounded-md border border-card-border bg-transparent px-3 py-2 text-sm text-primary placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary',
                      errors.description && 'border-vibrant-red',
                    )}
                    rows={2}
                    maxLength={255}
                  />
                  {errors.description && (
                    <p className="mt-1 text-sm text-vibrant-red">
                      {errors.description.message}
                    </p>
                  )}
                </div>

                {/* Thumbnail */}
                <div>
                  <Label>Thumbnail</Label>
                  <div className="mt-2 flex flex-col gap-3 md:flex-row md:items-center">
                    <div className="flex w-full items-center gap-2 rounded-md border border-dashed border-card-border bg-transparent hover:bg-card-background">
                      <input
                        type="file"
                        accept="image/png,image/jpeg"
                        id="thumbnailUpload"
                        className="hidden"
                        onChange={e => {
                          const file = e.target.files?.[0];
                          if (!file) return;
                          handleThumbnailFileSelect(file);
                          e.target.value = '';
                        }}
                      />
                      <Button
                        type="button"
                        variant="secondary"
                        className="h-16 w-full bg-transparent"
                        onClick={() =>
                          document.getElementById('thumbnailUpload')?.click()
                        }
                      >
                        Choose file
                      </Button>
                    </div>
                  </div>
                  {errors.thumbnailUrl && (
                    <p className="mt-1 text-sm text-vibrant-red">
                      {errors.thumbnailUrl.message as string}
                    </p>
                  )}
                  {thumbnailPreviewUrl || thumbnailUrl ? (
                    <div className="mt-3 flex items-center gap-6">
                      <img
                        src={
                          thumbnailPreviewUrl ||
                          FILE_PATH_PREFIX + thumbnailUrl ||
                          ''
                        }
                        alt="Thumbnail preview"
                        className="h-20 w-32 rounded-md border border-card-border object-cover"
                      />
                      {thumbnailFile || thumbnailUrl ? (
                        <Button
                          type="button"
                          variant="outline"
                          onClick={clearThumbnail}
                        >
                          <Trash className="size-4 text-vibrant-red" />
                        </Button>
                      ) : null}
                    </div>
                  ) : null}
                  <p className="mt-1 text-xs text-muted-foreground">
                    JPG/PNG, max 2MB
                  </p>
                </div>

                {/* Email Configuration */}
                <div className="grid gap-4 md:grid-cols-3">
                  <div>
                    <Label htmlFor="emailType">
                      {isSmsTemplate ? 'SMS Type' : 'Email Type'}
                    </Label>
                    <Input
                      id="emailType"
                      value={
                        emailType === 'SPEAR_PHISH'
                          ? 'Spear Phish'
                          : 'Standard Phish'
                      }
                      disabled
                    />
                    <p className="mt-1 text-xs text-muted-foreground">
                      Auto-selected
                    </p>
                  </div>
                  <div>
                    <Label htmlFor="payloadType">
                      Payload Type <span className="text-vibrant-red">*</span>
                    </Label>
                    <SearchSelect
                      items={payloadTypes.map(type => ({
                        value: type.id,
                        label: type.name,
                      }))}
                      placeholder="Select payload type"
                      value={watch('payloadType')?.id}
                      onValueChange={(value: string) => {
                        const item = payloadTypes.find(
                          type => type.id === value,
                        );
                        if (item) {
                          setValue(
                            'payloadType',
                            { id: item.id, name: item.name },
                            { shouldValidate: true },
                          );
                        }
                      }}
                    />
                    {errors.payloadType && (
                      <p className="mt-1 text-sm text-vibrant-red">
                        {errors.payloadType.id?.message ||
                          errors.payloadType.name?.message ||
                          errors.payloadType.message}
                      </p>
                    )}
                  </div>
                  <div>
                    <Label htmlFor="difficultyLevel">
                      Difficulty <span className="text-vibrant-red">*</span>
                    </Label>
                    <Select
                      value={watch('difficultyLevel')?.id}
                      onValueChange={value => {
                        const item = difficultiesList.find(
                          level => level.id === value,
                        );
                        if (item) {
                          setValue(
                            'difficultyLevel',
                            { id: item.id, name: item.name },
                            { shouldValidate: true },
                          );
                        }
                      }}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select difficulty level" />
                      </SelectTrigger>
                      <SelectContent>
                        {difficultiesList.map(item => (
                          <SelectItem key={item.id} value={item.id}>
                            {item.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {errors.difficultyLevel && (
                      <p className="mt-1 text-sm text-vibrant-red">
                        {errors.difficultyLevel.id?.message ||
                          errors.difficultyLevel.name?.message ||
                          errors.difficultyLevel.message}
                      </p>
                    )}
                  </div>
                </div>

                {/* Landing Pages */}
                <div>
                  <Label>
                    Landing Page <span className="text-vibrant-red">*</span>
                  </Label>
                  <Controller
                    name="landingPageIds"
                    control={control}
                    render={({ field }) => (
                      <CustomSelect
                        isMulti
                        isSearchable
                        isLoading={landingPagesLoading}
                        placeholder="Select landing page(s)"
                        name="landingPageIds"
                        data={landingPageOptions}
                        customClassName="mt-1"
                        value={landingPageOptions.filter(o =>
                          (field.value ?? []).includes(o.value),
                        )}
                        handleChange={newValue => {
                          const selected =
                            newValue as TMultiValue<ICustomSelectOption>;
                          field.onChange(selected?.map(o => o.value) ?? []);
                        }}
                      />
                    )}
                  />
                  {errors.landingPageIds && (
                    <p className="mt-1 text-sm text-vibrant-red">
                      {errors.landingPageIds.message}
                    </p>
                  )}
                </div>

                {isSmsTemplate && (
                  <SmsDeliveryRulesCard
                    value={smsDeliveryMode}
                    onChange={handleSmsDeliveryModeChange}
                  />
                )}

                {/* Template body */}
                <div>
                  <div className="flex items-center justify-between gap-3">
                    <Label>
                      {isSmsTemplate ? 'SMS Message' : 'Email Body'}{' '}
                      <span className="text-vibrant-red">*</span>
                    </Label>
                    {!isSmsTemplate && (
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        className="mb-2"
                        onClick={() => setShowFieldsHelpModal(true)}
                      >
                        <HelpCircle className="size-4" />
                        View Available Fields
                      </Button>
                    )}
                  </div>
                  <Controller
                    name="emailBody"
                    control={control}
                    render={({ field }) =>
                      isSmsTemplate ? (
                        <SmsTemplateEditor
                          value={field.value}
                          onChange={field.onChange}
                          error={errors.emailBody?.message}
                          deliveryMode={smsDeliveryMode}
                          phishingPreviewUrl={phishingPreviewUrl}
                        />
                      ) : (
                        <EmailTemplateEditor
                          value={field.value}
                          onChange={field.onChange}
                          onAIGenerate={handleEditorAIFix}
                        />
                      )
                    }
                  />
                  {isSmsTemplate && (
                    <p className="mt-2 text-sm text-muted-foreground">
                      {trackingDomain ? (
                        <>
                          Tracking domain:{' '}
                          <span className="font-medium text-primary">
                            {trackingDomain}
                          </span>
                        </>
                      ) : (
                        'Select a landing page to set the tracking domain.'
                      )}
                    </p>
                  )}
                  {!isSmsTemplate && errors.emailBody && (
                    <p className="mt-1 text-sm text-vibrant-red">
                      {errors.emailBody.message}
                    </p>
                  )}
                </div>

                {/* Employee Data Required */}
                <Controller
                  name="employeeDataRequired"
                  control={control}
                  render={({ field }) => (
                    <EmployeeDataFieldSelector
                      value={field.value as EmployeeDataField[]}
                      onChange={field.onChange}
                      error={errors.employeeDataRequired?.message}
                    />
                  )}
                />

                {/* Tags */}
                <div>
                  <Label>Tags</Label>
                  <Controller
                    name="tags"
                    control={control}
                    render={({ field }) => (
                      <TagSelector
                        value={field.value || []}
                        onChange={field.onChange}
                        error={errors.tags?.message}
                        className="mt-2"
                      />
                    )}
                  />
                </div>

                {/* Location and Language */}
                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <Label htmlFor="serviceLocation">Service Location</Label>
                    <Input
                      id="serviceLocation"
                      {...register('serviceLocation')}
                      placeholder="e.g., United States"
                      maxLength={255}
                    />
                  </div>
                  <div>
                    <Label htmlFor="language">Language</Label>
                    <SearchSelect
                      items={languages.map(item => ({
                        value: item.displayName,
                        label: item.displayName,
                      }))}
                      placeholder="Select language"
                      value={watch('language')}
                      onValueChange={(value: string) =>
                        setValue('language', value, {
                          shouldValidate: true,
                        })
                      }
                    />
                  </div>
                </div>

                {/* Attachments (email only) */}
                {!isSmsTemplate && (
                  <div>
                    <Label>Attachments</Label>
                    <p className="mb-2 text-xs text-muted-foreground">
                      JPG/PNG only, max 2MB each, max 5 total
                    </p>
                    <div className="flex w-full items-center gap-2 rounded-md border border-dashed border-card-border bg-transparent hover:bg-card-background">
                      <input
                        type="file"
                        accept="application/pdf,image/png,image/jpeg"
                        multiple
                        onChange={e => {
                          const files = e.target.files;
                          if (files) {
                            for (const file of files) {
                              handleUploadAttachment(file);
                            }
                          }
                          e.target.value = '';
                        }}
                        className="hidden"
                        id="attachmentUpload"
                      />
                      <Button
                        type="button"
                        variant="secondary"
                        className="h-16 w-full bg-transparent"
                        onClick={() =>
                          document.getElementById('attachmentUpload')?.click()
                        }
                      >
                        Upload Files <Upload className="size-4" />
                      </Button>
                    </div>
                    <div className="mt-2 space-y-2">
                      {attachment.files.map(file => (
                        <div
                          key={file.name}
                          className="flex items-center justify-between rounded-md border border-card-border px-3 py-2"
                        >
                          <p>{file.name}</p>
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() =>
                              handleDeletePendingAttachment(file.name)
                            }
                          >
                            <Trash className="size-4 text-vibrant-red" />
                          </Button>
                        </div>
                      ))}
                      {attachment.links.map(link => (
                        <div
                          key={link}
                          className="flex items-center justify-between rounded-md border border-card-border px-3 py-2"
                        >
                          <p className="truncate text-sm">
                            {FILE_PATH_PREFIX + link}
                          </p>
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => handleDeleteExistingAttachment(link)}
                          >
                            <Trash className="size-4 text-vibrant-red" />
                          </Button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Form Actions */}
                <div className="flex items-center justify-end gap-4 border-t border-card-border pt-6">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={navigateToLibrary}
                  >
                    Cancel
                  </Button>
                  <Button type="submit" disabled={!isValid || isBusy}>
                    {isBusy ? (
                      <>
                        <Loader2 className="mr-2 size-4 animate-spin" />
                        {loadingExisting ? 'Loading...' : 'Saving...'}
                      </>
                    ) : (
                      <>
                        <Save className="mr-2 size-4" />
                        {isEditMode ? 'Update Template' : 'Save Template'}
                      </>
                    )}
                  </Button>
                </div>
              </CardContent>
            </Card>
          </form>
        </div>

        {/* Preview Section */}
        <div className="lg:col-span-1">
          <TemplatePreview
            templateName={templateName}
            emailSubject={emailSubject}
            emailBody={emailBody}
            attachments={attachment.files}
            variant={isSmsTemplate ? 'sms' : 'email'}
            smsDeliveryMode={isSmsTemplate ? smsDeliveryMode : undefined}
            phishingPreviewUrl={isSmsTemplate ? phishingPreviewUrl : undefined}
          />
        </div>
      </div>

      {/* AI Generate Modal */}
      {!isEditMode && (
        <AIGenerateModal
          isOpen={showAIModal}
          onClose={() => setShowAIModal(false)}
          onGenerate={handleAIGenerate}
          isGenerating={creating}
        />
      )}

      {/* Available Fields Help Modal (email only) */}
      {!isSmsTemplate && (
        <EmailTemplateFieldsHelpModal
          isOpen={showFieldsHelpModal}
          onClose={() => setShowFieldsHelpModal(false)}
        />
      )}
    </div>
  );
};

export default EmailTemplateCreate;
