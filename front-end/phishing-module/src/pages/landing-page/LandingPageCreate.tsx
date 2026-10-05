import { openHtmlInNewTab } from 'utils/OpenHtmlPreview';
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
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Textarea } from 'common/Textarea';
import TagSelector from 'features/email-template/TagSelector';
import AILandingPageModal from 'features/landing-page/AILandingPageModal';
import DataCaptureConfig from 'features/landing-page/DataCaptureConfig';
import DevicePreviewToggle from 'features/landing-page/DevicePreviewToggle';
import ImportSiteModal from 'features/landing-page/ImportSiteModal';
import LandingPageHtmlEditor from 'features/landing-page/LandingPageHtmlEditor';
import PageTypeSelector from 'features/landing-page/PageTypeSelector';
import useLandingPages from 'hooks/UseLandingPages';
import { useUploader } from 'hooks/UseUploader';
import {
  ExternalLink,
  Eye,
  Globe,
  LayoutTemplate,
  Loader2,
  RefreshCw,
  Save,
  Sparkles,
  Trash,
} from 'lucide-react';
import { toIdName } from 'models/EmailTemplate';
import { IDomain, IDomainListResponse } from 'models/Domain';
import { FileType, IResponse } from 'models/Global';
import type { ILandingPageFixHtmlRequest } from 'models/LandingPage';
import {
  DEVICE_SIZES,
  DeviceType,
  IAILandingPageParams,
  IImportedSite,
  ILandingPageCreateForm,
  ILandingPageUpdateForm,
  LandingPageType,
} from 'models/LandingPage';
import { useCallback, useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  LandingPageCreateSchema,
  TAILandingPageForm,
  TLandingPageCreateForm,
  landingPageCreateDefaultValues,
} from 'schemas/LandingPageSchema';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { cn } from 'utils/Helper';
import {
  getLandingPageChannelFromPath,
  getLandingPageLibraryUrl,
} from 'utils/ListNavigation';
import { getSimulationLabel } from 'utils/SimulationChannel';
import useDropDown from 'hooks/UseDropDown';
import { useAPI } from 'hooks/UseAPI';
import { IDropdownItem } from 'models/DropDown';
import SearchSelect from 'components/SearchSelect';
import IconBackButton from 'components/IconBackButton';

/**
 * Create Landing Page page
 * Supports manual creation, website import, and AI-powered generation
 * Based on Task-05 requirements
 */
const LandingPageCreate = () => {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const channel = getLandingPageChannelFromPath(pathname);
  const simulationLabel = getSimulationLabel(channel);
  const libraryPath = getLandingPageLibraryUrl(channel);
  const { id } = useParams<{ id?: string }>();
  const isEditMode = !!id;
  const { uploadFile } = useUploader();
  const {
    createLandingPage,
    updateLandingPage,
    getLandingPageById,
    getLandingPagePreview,
    importWebsite,
    generateAILandingPage,
    fixLandingPageHtml,
    validateHtml,
    creating,
    updating,
    importing,
    generating,
  } = useLandingPages();

  const { fetchLandingPageCategories, fetchDifficulties } = useDropDown();
  const apiClient = useAPI();

  const MAX_UPLOAD_SIZE_BYTES = 2 * 1024 * 1024; // 2MB

  const [landingPageCategories, setLandingPageCategories] = useState<
    IDropdownItem[]
  >([]);
  const [difficultiesList, setDifficultiesList] = useState<IDropdownItem[]>([]);
  const [domainOptions, setDomainOptions] = useState<IDomain[]>([]);

  // Modal states
  const [showImportModal, setShowImportModal] = useState(false);
  const [showAIModal, setShowAIModal] = useState(false);

  // Preview state
  const [previewDevice, setPreviewDevice] = useState<DeviceType>('desktop');
  const [showPreview, setShowPreview] = useState(true);

  // Thumbnail file (uploaded only on submit)
  const [thumbnailFile, setThumbnailFile] = useState<File | null>(null);
  const [thumbnailPreviewUrl, setThumbnailPreviewUrl] = useState<string | null>(
    null,
  );
  const [loadingExisting, setLoadingExisting] = useState(false);

  // Form setup
  const {
    register,
    control,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors, isValid, isSubmitting },
  } = useForm<TLandingPageCreateForm>({
    resolver: zodResolver(LandingPageCreateSchema),
    defaultValues: landingPageCreateDefaultValues,
    mode: 'onChange',
  });

  // Watch values for preview and conditional rendering
  const pageType = watch('pageType');
  const htmlContent = watch('htmlContent');
  const captureSubmittedData = watch('captureSubmittedData');
  const captureFields = watch('captureFields');
  const redirectUrl = watch('redirectUrl');
  const name = watch('name');
  const statusValue = watch('status');
  const thumbnailUrl = watch('thumbnailUrl');
  const trackingDomainId = watch('trackingDomainId');

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

  useEffect(() => {
    const loadLandingPageCategories = async () => {
      const data = await fetchLandingPageCategories();
      setLandingPageCategories(data.items);
      if (isEditMode) return;
      const defaultItem =
        data.items.find(cat => cat.isDefault) ?? data.items[0];
      if (defaultItem) {
        setValue(
          'category',
          { id: defaultItem.id, name: defaultItem.name },
          { shouldValidate: true, shouldDirty: true },
        );
      }
    };
    loadLandingPageCategories();
  }, [fetchLandingPageCategories, isEditMode, setValue]);

  useEffect(() => {
    const loadDifficulties = async () => {
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
    loadDifficulties();
  }, [fetchDifficulties, isEditMode, setValue]);

  useEffect(() => {
    const loadDomains = async () => {
      try {
        const response = (await apiClient.get(
          `${API_END_POINTS.DOMAIN_LIST}offset=0&pageSize=1000&status=VERIFIED&status=VERIFIED_AND_LOCKED`,
        )) as IResponse<IDomainListResponse>;
        if (response?.data?.items) {
          setDomainOptions(response.data.items);
        }
      } catch (error) {
        console.error('Error fetching domains:', error);
      }
    };
    loadDomains();
  }, [apiClient]);

  const handleEditorAIFix = useCallback(
    async (params: ILandingPageFixHtmlRequest) => {
      const result = await fixLandingPageHtml(params);
      return result ?? undefined;
    },
    [fixLandingPageHtml],
  );

  // Load existing landing page in edit mode (mirrors email template edit flow)
  useEffect(() => {
    const loadExisting = async () => {
      if (!isEditMode || !id) return;
      setLoadingExisting(true);
      try {
        const [details, preview, categoryData, difficultyData] =
          await Promise.all([
            getLandingPageById(id),
            getLandingPagePreview(id),
            fetchLandingPageCategories(),
            fetchDifficulties(),
          ]);

        if (!details || !preview) return;

        setLandingPageCategories(categoryData.items);
        setDifficultiesList(difficultyData.items);

        reset({
          ...landingPageCreateDefaultValues,
          name: details.name ?? '',
          description: details.description ?? '',
          status: details.status as TLandingPageCreateForm['status'],
          pageType: details.pageType ?? LandingPageType.LANDING_PAGE,
          category: toIdName(details.category, categoryData.items),
          difficultyLevel: toIdName(
            details.difficultyLevel,
            difficultyData.items,
          ),
          htmlContent: preview.htmlContent ?? '',
          thumbnailUrl: details.thumbnailUrl ?? '',
          websiteUrl: details.websiteUrl ?? '',
          tags: details.tags ?? [],
          captureSubmittedData: !!preview.captureSubmittedData,
          captureFields: preview.captureFields ?? [],
          redirectUrl: preview.redirectUrl ?? details.redirectUrl ?? '',
          trackingDomainId: details.trackingDomainId ?? '',
        });

        setThumbnailFile(null); // clear "new upload" state; keep existing thumbnailUrl
      } finally {
        setLoadingExisting(false);
      }
    };

    loadExisting();
  }, [
    fetchDifficulties,
    fetchLandingPageCategories,
    getLandingPageById,
    getLandingPagePreview,
    id,
    isEditMode,
    reset,
  ]);

  // Navigate to library
  const navigateToLibrary = () => {
    navigate(libraryPath);
  };

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

  // Handle form submission
  const handleFormSubmit = async (data: TLandingPageCreateForm) => {
    // Validate HTML before saving (AC-12)
    if (data.htmlContent) {
      const validation = await validateHtml(data.htmlContent);
      if (validation && !validation.isValid) {
        // Show warnings but allow saving
        console.warn('HTML validation warnings:', validation.warnings);
      }
    }

    let thumbnailUrlFinal = data.thumbnailUrl || '';
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
      const updateData: ILandingPageUpdateForm = {
        name: data.name,
        description: data.description || '',
        status: data.status,
        category: data.category,
        difficultyLevel: data.difficultyLevel,
        htmlContent: data.htmlContent,
        thumbnailUrl: thumbnailUrlFinal || undefined,
        tags: data.tags || [],
        captureSubmittedData: data.captureSubmittedData,
        captureFields: data.captureFields || [],
        redirectUrl: data.redirectUrl || '',
        trackingDomainId: data.trackingDomainId,
      };

      const result = await updateLandingPage(id, updateData);
      if (result) navigateToLibrary();
      return;
    }

    // Convert form data to API format
    const apiData: ILandingPageCreateForm = {
      name: data.name,
      description: data.description || '',
      status: data.status,
      pageType: data.pageType,
      category: data.category,
      difficultyLevel: data.difficultyLevel,
      htmlContent: data.htmlContent,
      thumbnailUrl: thumbnailUrlFinal || undefined,
      websiteUrl: data.websiteUrl || undefined,
      tags: data.tags || [],
      captureSubmittedData: data.captureSubmittedData,
      captureFields: data.captureFields || [],
      redirectUrl: data.redirectUrl || '',
      trackingDomainId: data.trackingDomainId,
    };

    const result = await createLandingPage(apiData);
    if (result) navigateToLibrary();
  };

  // Handle website import (AC-03, AC-04)
  const handleImport = useCallback(
    async (url: string, includeAssets: boolean) => {
      return await importWebsite(url, includeAssets);
    },
    [importWebsite],
  );

  // Handle imported content (AC-05)
  const handleUseImportedContent = useCallback(
    (importedSite: IImportedSite) => {
      setValue('htmlContent', importedSite.htmlContent);
      if (importedSite.originalUrl) {
        setValue('websiteUrl', importedSite.originalUrl);
      }
      if (importedSite.thumbnailUrl) {
        setValue('thumbnailUrl', importedSite.thumbnailUrl);
        setThumbnailFile(null);
      }
      // Auto-enable capture if login form detected
      if (importedSite.hasLoginForm) {
        setValue('captureSubmittedData', true);
        if (importedSite.detectedFormFields.length > 0) {
          setValue('captureFields', importedSite.detectedFormFields);
        }
      }
      setShowImportModal(false);
    },
    [setValue],
  );

  // Handle AI generation (AC-06, AC-07)
  const handleAIGenerate = useCallback(
    async (params: TAILandingPageForm): Promise<boolean> => {
      const result = await generateAILandingPage(
        params as IAILandingPageParams,
      );
      if (result) {
        setShowAIModal(false);
        navigate(libraryPath);
        return true;
      }
      return false;
    },
    [generateAILandingPage, libraryPath, navigate],
  );

  // Open preview in new tab
  const handleOpenInNewTab = () => {
    if (!htmlContent) return;
    openHtmlInNewTab(htmlContent);
  };

  const isSaving = (isEditMode ? updating : creating) || isSubmitting;
  const isBusy = isSaving || loadingExisting;
  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="space-y-3">
        <IconBackButton
          onClick={navigateToLibrary}
          label="Back to Landing Pages"
        />
        <div className="flex items-center justify-between">
          <div>
            <h1 className="flex items-center gap-2 text-2xl font-semibold text-primary">
              <LayoutTemplate className="size-7 text-primary" />
              {isEditMode ? 'Edit Landing Page' : 'Create Landing Page'}
            </h1>
            <p className="mt-1 text-muted-foreground">
              {isEditMode
                ? `Update your ${simulationLabel.toLowerCase()} landing page`
                : `Create a new ${simulationLabel.toLowerCase()} landing page for your campaigns`}
            </p>
          </div>
          <div className="flex items-center gap-3">
            {!isEditMode && (
              <>
                <Button
                  variant="outline"
                  onClick={() => setShowImportModal(true)}
                  disabled={loadingExisting}
                >
                  <Globe className="mr-2 size-4" />
                  Import Website
                </Button>
                <Button
                  variant="outline"
                  onClick={() => setShowAIModal(true)}
                  disabled={loadingExisting}
                >
                  <Sparkles className="mr-2 size-4" />
                  Create with AI
                </Button>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* Form Section */}
        <div className="space-y-6">
          <form
            onSubmit={handleSubmit(handleFormSubmit)}
            id="landing-page-form"
          >
            {/* Page Type Selection (AC-01) */}
            <Card className="mb-6">
              <CardHeader>
                <CardTitle>Page Type</CardTitle>
                <CardDescription>
                  Select the type of landing page you want to create
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Controller
                  name="pageType"
                  control={control}
                  render={({ field }) => (
                    <PageTypeSelector
                      selectedType={field.value}
                      onTypeChange={field.onChange}
                      // disabled={isEditMode}
                    />
                  )}
                />
              </CardContent>
            </Card>

            {/* Page Details (AC-02) */}
            <Card className="mb-6">
              <CardHeader>
                <CardTitle>Page Details</CardTitle>
                <CardDescription>
                  Enter the basic information for your landing page
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {/* Name */}
                <div>
                  <Label htmlFor="name">
                    Page Name <span className="text-vibrant-red">*</span>
                  </Label>
                  <Input
                    id="name"
                    {...register('name')}
                    placeholder="Enter page name"
                    className={cn(errors.name && 'border-vibrant-red')}
                    maxLength={100}
                  />
                  {errors.name && (
                    <p className="mt-1 text-sm text-vibrant-red">
                      {errors.name.message}
                    </p>
                  )}
                </div>

                {/* Description */}
                <div>
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    {...register('description')}
                    placeholder="Brief description of the landing page..."
                    className={errors.description && 'border-vibrant-red'}
                    maxLength={255}
                  />
                  {errors.description && (
                    <p className="mt-1 text-sm text-vibrant-red">
                      {errors.description.message}
                    </p>
                  )}
                </div>

                {/* Status */}
                <div>
                  <Label htmlFor="status">
                    Status <span className="text-vibrant-red">*</span>
                  </Label>
                  <Select
                    value={statusValue}
                    onValueChange={value =>
                      setValue(
                        'status',
                        value as TLandingPageCreateForm['status'],
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

                {/* Thumbnail */}
                <div>
                  <Label>Thumbnail</Label>
                  <div className="flex w-full items-center gap-2 rounded-md border border-dashed border-card-border bg-transparent px-2 hover:bg-card-background">
                    <input
                      type="file"
                      accept="image/png,image/jpeg"
                      id="landingThumbnailUpload"
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
                        document
                          .getElementById('landingThumbnailUpload')
                          ?.click()
                      }
                    >
                      Choose file
                    </Button>
                  </div>
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
                    JPG/PNG, max 2MB. Uploaded when you click Save.
                  </p>
                </div>

                {/* Tracking Domain */}
                <div>
                  <Label htmlFor="trackingDomainId">
                    Tracking Domain <span className="text-vibrant-red">*</span>
                  </Label>
                  <SearchSelect
                    items={domainOptions.map(domain => ({
                      value: domain.domainId,
                      label: domain.domain,
                    }))}
                    value={trackingDomainId}
                    onValueChange={value =>
                      setValue('trackingDomainId', value, {
                        shouldValidate: true,
                      })
                    }
                    placeholder="Select tracking domain"
                  />
                  {errors.trackingDomainId && (
                    <p className="mt-1 text-sm text-vibrant-red">
                      {errors.trackingDomainId.message}
                    </p>
                  )}
                </div>

                {/* Category & Difficulty */}
                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <Label htmlFor="category">
                      Category <span className="text-vibrant-red">*</span>
                    </Label>
                    <SearchSelect
                      items={landingPageCategories.map(cat => ({
                        value: cat.id,
                        label: cat.name,
                      }))}
                      placeholder="Select category"
                      value={watch('category')?.id}
                      onValueChange={(value: string) => {
                        const item = landingPageCategories.find(
                          cat => cat.id === value,
                        );
                        if (item) {
                          setValue(
                            'category',
                            { id: item.id, name: item.name },
                            { shouldValidate: true },
                          );
                        }
                      }}
                    />
                    {errors.category && (
                      <p className="mt-1 text-sm text-vibrant-red">
                        {errors.category.id?.message ||
                          errors.category.name?.message ||
                          errors.category.message}
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
                        className="mt-2"
                      />
                    )}
                  />
                </div>
              </CardContent>
            </Card>

            {/* HTML Content */}
            <Card className="mb-6">
              <CardHeader>
                <CardTitle>
                  HTML Content <span className="text-vibrant-red">*</span>
                </CardTitle>
                <CardDescription>
                  Enter or paste your landing page HTML content in text editor
                  source code mode
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Controller
                  name="htmlContent"
                  control={control}
                  render={({ field }) => (
                    <LandingPageHtmlEditor
                      value={field.value}
                      onChange={field.onChange}
                      error={errors.htmlContent?.message}
                      onAIGenerate={handleEditorAIFix}
                    />
                  )}
                />
              </CardContent>
            </Card>

            {/* Data Capture Configuration (AC-09) */}
            <Card className="mb-6">
              <CardHeader>
                <CardTitle>Data Capture</CardTitle>
                <CardDescription>
                  Configure form data capture settings
                </CardDescription>
              </CardHeader>
              <CardContent>
                <DataCaptureConfig
                  enabled={captureSubmittedData || false}
                  onEnabledChange={enabled =>
                    setValue('captureSubmittedData', enabled)
                  }
                  selectedFields={captureFields || []}
                  onFieldsChange={fields => setValue('captureFields', fields)}
                  redirectUrl={redirectUrl || ''}
                  onRedirectUrlChange={url => setValue('redirectUrl', url)}
                  redirectUrlError={errors.redirectUrl?.message}
                />
              </CardContent>
            </Card>

            {/* Form Actions */}
            <div className="flex items-center justify-end gap-4">
              <Button
                type="button"
                variant="outline"
                onClick={navigateToLibrary}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={!isValid || isBusy}>
                {isSaving ? (
                  <>
                    <Loader2 className="mr-2 size-4 animate-spin" />
                    Saving...
                  </>
                ) : (
                  <>
                    <Save className="mr-2 size-4" />
                    {isEditMode ? 'Update Landing Page' : 'Save Landing Page'}
                  </>
                )}
              </Button>
            </div>
          </form>
        </div>

        {/* Preview Section (AC-10, AC-11) */}
        <div className="lg:sticky lg:top-4 lg:self-start">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2">
                  <Eye className="size-5 text-primary" />
                  Preview
                </CardTitle>
                <div className="flex items-center gap-2">
                  <DevicePreviewToggle
                    selectedDevice={previewDevice}
                    onDeviceChange={setPreviewDevice}
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={handleOpenInNewTab}
                    disabled={!htmlContent}
                    title="Open in new tab"
                  >
                    <ExternalLink className="size-4" />
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => setShowPreview(!showPreview)}
                    title={showPreview ? 'Hide preview' : 'Show preview'}
                  >
                    <RefreshCw className="size-4" />
                  </Button>
                </div>
              </div>
              {name && <CardDescription>Preview: {name}</CardDescription>}
            </CardHeader>
            <CardContent>
              {showPreview ? (
                <div
                  className="mx-auto overflow-hidden rounded-lg border border-primary bg-white transition-all duration-300"
                  style={{
                    width: DEVICE_SIZES[previewDevice].width,
                    maxWidth: '100%',
                    height: '500px',
                  }}
                >
                  {htmlContent ? (
                    <iframe
                      srcDoc={htmlContent}
                      title="Landing Page Preview"
                      className="size-full border-0"
                      sandbox="allow-same-origin"
                    />
                  ) : (
                    <div className="flex h-full flex-col items-center justify-center text-muted-foreground">
                      <LayoutTemplate className="mb-4 size-12 opacity-30" />
                      <p>Enter HTML content to see preview</p>
                      <p className="mt-2 text-sm">
                        Or use &quot;Import Website&quot; or &quot;Create with
                        AI&quot;
                      </p>
                    </div>
                  )}
                </div>
              ) : (
                <div className="flex h-[500px] items-center justify-center text-muted-foreground">
                  <p>Preview hidden. Click refresh to show.</p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      {!isEditMode && (
        <>
          {/* Import Site Modal (AC-03, AC-04, AC-05) */}
          <ImportSiteModal
            isOpen={showImportModal}
            onClose={() => setShowImportModal(false)}
            onImport={handleImport}
            onUseContent={handleUseImportedContent}
            importing={importing}
          />

          {/* AI Generation Modal (AC-06, AC-07, AC-08) */}
          {showAIModal && (
            <AILandingPageModal
              isOpen={showAIModal}
              onClose={() => setShowAIModal(false)}
              onGenerate={handleAIGenerate}
              generating={generating}
              defaultPageType={pageType}
            />
          )}
        </>
      )}
    </div>
  );
};

export default LandingPageCreate;
