import { useState, useEffect, useCallback, useMemo } from 'react';
import { Controller, SubmitHandler, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Sparkles, Loader2, Trash } from 'lucide-react';
import { toast } from 'react-toastify';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Textarea } from 'common/Textarea';
import TagSelector from 'features/email-template/TagSelector';
import VoiceInputControl from 'features/email-template/VoiceInputControl';
import useVoiceRecording from 'hooks/UseVoiceRecording';
import { useUploader } from 'hooks/UseUploader';
import {
  AI_GENERATE_PROVIDER_TYPES,
  IIdName,
} from 'models/EmailTemplate';
import {
  GENERATION_MODES,
  LAYOUT_STYLES,
  LandingPageType,
} from 'models/LandingPage';
import { IDomain, IDomainListResponse } from 'models/Domain';
import { IAIModelItem } from 'models/AiProvider';
import {
  AILandingPageSchema,
  DefaultAILandingPageValues,
  emptyIdName,
  TAILandingPageForm,
} from 'schemas/LandingPageSchema';
import { useAPI } from 'hooks/UseAPI';
import { FileType, IActiveLanguage, IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import SearchSelect from 'components/SearchSelect';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import useDropDown from 'hooks/UseDropDown';
import { IDropdownItem } from 'models/DropDown';

type IdNameFieldError = {
  id?: { message?: string };
  name?: { message?: string };
  message?: string;
};

const idNameFieldError = (error?: IdNameFieldError) =>
  error?.id?.message || error?.name?.message || error?.message;

const pickIdName = (
  items: Array<{ id: string; name: string }>,
  id: string,
): IIdName => {
  const item = items.find(i => i.id === id);
  return item ? { id: item.id, name: item.name } : emptyIdName;
};

interface AILandingPageModalProps {
  isOpen: boolean;
  onClose: () => void;
  onGenerate: (params: TAILandingPageForm) => Promise<boolean>;
  generating?: boolean;
  defaultPageType?: LandingPageType;
}

export const AILandingPageModal = ({
  isOpen,
  onClose,
  onGenerate,
  generating = false,
  defaultPageType = LandingPageType.LANDING_PAGE,
}: AILandingPageModalProps) => {
  const [inputMode, setInputMode] = useState<'VOICE' | 'CONTEXT'>('VOICE');
  const [languages, setLanguages] = useState<IActiveLanguage[]>([]);
  const [languagesLoading, setLanguagesLoading] = useState(false);
  const [aiModels, setAiModels] = useState<IAIModelItem[]>([]);
  const [modelsLoading, setModelsLoading] = useState(false);
  const [thumbnailFile, setThumbnailFile] = useState<File | null>(null);
  const [thumbnailPreviewUrl, setThumbnailPreviewUrl] = useState<string | null>(
    null,
  );
  const [domainOptions, setDomainOptions] = useState<IDomain[]>([]);
  const MAX_UPLOAD_SIZE_BYTES = 2 * 1024 * 1024;
  const apiClient = useAPI();
  const { uploadFile, loading: uploadingThumbnail } = useUploader();

  const {
    isRecording,
    isProcessing,
    transcript,
    detectedLanguage,
    confidence,
    error: voiceError,
    startRecording,
    stopRecording,
    clearTranscript,
    updateTranscript,
  } = useVoiceRecording();

  const {
    fetchLandingPageCategories,
    fetchTones,
    fetchBrands,
    fetchCallToActions,
    fetchDataCaptureTypes,
    fetchDepartments,
    fetchEmotionalTriggers,
    fetchUrgencyLevels,
    fetchDifficulties,
  } = useDropDown();

  const [dropdowns, setDropdowns] = useState<{
    landingPageCategories: IDropdownItem[];
    tones: IDropdownItem[];
    brands: IDropdownItem[];
    callToActions: IDropdownItem[];
    dataCaptureTypes: IDropdownItem[];
    departments: IDropdownItem[];
    emotionalTriggers: IDropdownItem[];
    difficulties: IDropdownItem[];
    urgencyLevels: IDropdownItem[];
  }>({
    landingPageCategories: [],
    tones: [],
    brands: [],
    callToActions: [],
    dataCaptureTypes: [],
    departments: [],
    emotionalTriggers: [],
    difficulties: [],
    urgencyLevels: [],
  });

  const {
    register,
    control,
    handleSubmit,
    setValue,
    watch,
    reset,
    getValues,
    formState: { errors, isValid, isSubmitting },
  } = useForm<TAILandingPageForm>({
    resolver: zodResolver(AILandingPageSchema),
    defaultValues: {
      ...DefaultAILandingPageValues,
      pageType: defaultPageType,
    },
    mode: 'onChange',
  });

  const selectedLanguage = watch('inputLanguage');
  const providerType = watch('providerType');
  const generationMode = watch('generationMode');
  const thumbnailUrl = watch('thumbnailUrl') || '';
  const trackingDomainId = watch('trackingDomainId') || '';

  useEffect(() => {
    const loadLandingPageCategories = async () => {
      const data = await fetchLandingPageCategories();
      setDropdowns(prev => ({ ...prev, landingPageCategories: data.items }));
    };
    const loadTones = async () => {
      const data = await fetchTones();
      setDropdowns(prev => ({ ...prev, tones: data.items }));
    };
    const loadBrands = async () => {
      const data = await fetchBrands();
      setDropdowns(prev => ({ ...prev, brands: data.items }));
    };
    const loadCallToActions = async () => {
      const data = await fetchCallToActions();
      setDropdowns(prev => ({ ...prev, callToActions: data.items }));
    };
    const loadDataCaptureTypes = async () => {
      const data = await fetchDataCaptureTypes();
      setDropdowns(prev => ({ ...prev, dataCaptureTypes: data.items }));
    };
    const loadDepartments = async () => {
      const data = await fetchDepartments();
      setDropdowns(prev => ({ ...prev, departments: data.items }));
    };
    const loadEmotionalTriggers = async () => {
      const data = await fetchEmotionalTriggers();
      setDropdowns(prev => ({ ...prev, emotionalTriggers: data.items }));
    };
    const loadDifficulties = async () => {
      const data = await fetchDifficulties();
      setDropdowns(prev => ({ ...prev, difficulties: data.items }));
    };
    const loadUrgencyLevels = async () => {
      const data = await fetchUrgencyLevels();
      setDropdowns(prev => ({ ...prev, urgencyLevels: data.items }));
    };
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
    if (isOpen) {
      void loadLandingPageCategories();
      void loadTones();
      void loadBrands();
      void loadCallToActions();
      void loadDataCaptureTypes();
      void loadDepartments();
      void loadEmotionalTriggers();
      void loadDifficulties();
      void loadUrgencyLevels();
      void loadDomains();
    }
  }, [
    isOpen,
    apiClient,
    fetchLandingPageCategories,
    fetchTones,
    fetchBrands,
    fetchCallToActions,
    fetchDataCaptureTypes,
    fetchDepartments,
    fetchEmotionalTriggers,
    fetchDifficulties,
    fetchUrgencyLevels,
  ]);
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

  const fetchAiModels = useCallback(
    async (provider: (typeof AI_GENERATE_PROVIDER_TYPES)[number]) => {
      setModelsLoading(true);
      try {
        const queryString = `?providerType=${encodeURIComponent(provider)}`;
        const response = (await apiClient.get(
          `${API_END_POINTS.AI_MODELS}${queryString}`,
        )) as IResponse<IAIModelItem[]>;
        if (isSuccessResponse(response.statusCode)) {
          const list = (response.data ?? []).filter(m => m.active);
          setAiModels(list);
          if (list.length === 0) {
            setValue('model', '', { shouldValidate: true });
          }
        } else {
          setAiModels([]);
          toast.error(response.message || 'Failed to load AI models');
          setValue('model', '', { shouldValidate: true });
        }
      } catch (e) {
        console.error('Error fetching AI models:', e);
        setAiModels([]);
        toast.error('Failed to load AI models');
        setValue('model', '', { shouldValidate: true });
      } finally {
        setModelsLoading(false);
      }
    },
    [apiClient, setValue],
  );

  const voiceSupportedLanguages = useMemo((): Record<string, string> => {
    return Object.fromEntries(
      languages.map(lang => [lang.displayName, lang.displayName]),
    );
  }, [languages]);

  const fetchActiveLanguages = useCallback(async () => {
    setLanguagesLoading(true);
    try {
      const response = (await apiClient.get(
        API_END_POINTS.GET_ACTIVE_LANGUAGE_LIST,
      )) as IResponse<IActiveLanguage[]>;
      if (isSuccessResponse(response.statusCode)) {
        setLanguages(response.data ?? []);
      } else {
        toast.error(response.message || 'Failed to load languages');
        setLanguages([]);
      }
    } catch (e) {
      console.error('Error fetching active languages:', e);
      toast.error('Failed to load languages');
      setLanguages([]);
    } finally {
      setLanguagesLoading(false);
    }
  }, [apiClient]);

  useEffect(() => {
    if (!isOpen) return;
    void fetchActiveLanguages();
  }, [isOpen, fetchActiveLanguages]);

  useEffect(() => {
    if (!isOpen) return;
    void fetchAiModels(providerType);
  }, [isOpen, providerType, fetchAiModels]);

  useEffect(() => {
    if (!isOpen || dropdowns.landingPageCategories.length === 0) return;
    const current = getValues('category');
    if (
      current?.id &&
      dropdowns.landingPageCategories.some(c => c.id === current.id)
    ) {
      return;
    }
    const defaultItem =
      dropdowns.landingPageCategories.find(c => c.isDefault) ??
      dropdowns.landingPageCategories[0];
    if (defaultItem) {
      setValue(
        'category',
        { id: defaultItem.id, name: defaultItem.name },
        { shouldValidate: true },
      );
    }
  }, [isOpen, dropdowns.landingPageCategories, getValues, setValue]);

  useEffect(() => {
    if (!isOpen || dropdowns.difficulties.length === 0) return;
    const current = getValues('difficultyLevel');
    if (
      current?.id &&
      dropdowns.difficulties.some(d => d.id === current.id)
    ) {
      return;
    }
    const defaultItem =
      dropdowns.difficulties.find(d => d.isDefault) ?? dropdowns.difficulties[0];
    if (defaultItem) {
      setValue(
        'difficultyLevel',
        { id: defaultItem.id, name: defaultItem.name },
        { shouldValidate: true },
      );
    }
  }, [isOpen, dropdowns.difficulties, getValues, setValue]);

  useEffect(() => {
    if (!isOpen || dropdowns.urgencyLevels.length === 0) return;
    const current = getValues('urgencyLevel');
    if (current?.id && dropdowns.urgencyLevels.some(u => u.id === current.id)) {
      return;
    }
    const defaultItem =
      dropdowns.urgencyLevels.find(u => u.isDefault) ?? dropdowns.urgencyLevels[0];
    if (defaultItem) {
      const picked = { id: defaultItem.id, name: defaultItem.name };
      setValue('urgencyLevel', picked, { shouldValidate: true });
      setValue('generationOptions.urgencyLevel', picked, {
        shouldValidate: true,
      });
    }
  }, [isOpen, dropdowns.urgencyLevels, getValues, setValue]);

  useEffect(() => {
    if (!isOpen || dropdowns.departments.length === 0) return;
    const current = getValues('targetDepartment');
    if (current?.id && dropdowns.departments.some(d => d.id === current.id)) {
      return;
    }
    const defaultItem =
      dropdowns.departments.find(d => d.isDefault) ?? dropdowns.departments[0];
    if (defaultItem) {
      setValue(
        'targetDepartment',
        { id: defaultItem.id, name: defaultItem.name },
        { shouldValidate: true },
      );
    }
  }, [isOpen, dropdowns.departments, getValues, setValue]);

  useEffect(() => {
    if (!isOpen || dropdowns.emotionalTriggers.length === 0) return;
    const current = getValues('emotionalTrigger');
    if (
      current?.id &&
      dropdowns.emotionalTriggers.some(t => t.id === current.id)
    ) {
      return;
    }
    const defaultItem =
      dropdowns.emotionalTriggers.find(t => t.isDefault) ??
      dropdowns.emotionalTriggers[0];
    if (defaultItem) {
      const picked = { id: defaultItem.id, name: defaultItem.name };
      setValue('emotionalTrigger', picked, { shouldValidate: true });
      setValue('generationOptions.emotionalTrigger', picked, {
        shouldValidate: true,
      });
    }
  }, [isOpen, dropdowns.emotionalTriggers, getValues, setValue]);

  useEffect(() => {
    if (!isOpen || dropdowns.tones.length === 0) return;
    const current = getValues('generationOptions.tone');
    if (current?.id && dropdowns.tones.some(t => t.id === current.id)) {
      return;
    }
    const defaultItem =
      dropdowns.tones.find(t => t.isDefault) ?? dropdowns.tones[0];
    if (defaultItem) {
      setValue(
        'generationOptions.tone',
        { id: defaultItem.id, name: defaultItem.name },
        { shouldValidate: true },
      );
    }
  }, [isOpen, dropdowns.tones, getValues, setValue]);

  useEffect(() => {
    if (!isOpen) return;
    const currentModel = getValues('model');
    const hasCurrentValue = aiModels.some(m => m.name === currentModel);
    if (hasCurrentValue) return;
    setValue(
      'model',
      aiModels.find(m => m.default)?.name ?? aiModels[0]?.name ?? '',
      {
        shouldValidate: true,
      },
    );
  }, [isOpen, aiModels, getValues, setValue]);

  useEffect(() => {
    if (languages.length === 0) return;
    const current = getValues('inputLanguage');
    if (current && !languages.some(l => l.displayName === current)) {
      setValue('inputLanguage', '', { shouldValidate: true });
    }
    const genLang = getValues('generationOptions.language');
    if (genLang && !languages.some(l => l.displayName === genLang)) {
      setValue('generationOptions.language', '', { shouldValidate: true });
    }
  }, [languages, getValues, setValue]);

  useEffect(() => {
    if (transcript) {
      setValue('voiceInput', transcript, { shouldValidate: true });
      if (detectedLanguage) {
        setValue('inputLanguage', detectedLanguage, { shouldValidate: true });
      }
    }
  }, [transcript, detectedLanguage, setValue]);

  useEffect(() => {
    if (!isOpen) {
      reset({ ...DefaultAILandingPageValues, pageType: defaultPageType });
      clearTranscript();
      setInputMode('VOICE');
    }
  }, [isOpen, reset, clearTranscript, defaultPageType]);

  const handleFormSubmit = async (data: TAILandingPageForm) => {
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

    const success = await onGenerate({
      ...data,
      thumbnailUrl: thumbnailUrlFinal,
      generationOptions: {
        ...data.generationOptions,
        urgencyLevel: data.urgencyLevel,
        emotionalTrigger: data.emotionalTrigger,
      },
    });
    if (success) {
      handleClose();
    }
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

  const handleClose = () => {
    reset({ ...DefaultAILandingPageValues, pageType: defaultPageType });
    clearTranscript();
    setInputMode('VOICE');
    setThumbnailFile(null);
    onClose();
  };

  const handleStartRecording = () => {
    startRecording(selectedLanguage || undefined);
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="max-h-[90vh] w-4/5 overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Sparkles className="size-5 text-primary" />
            Generate Landing Page with AI
          </DialogTitle>
          <DialogDescription>
            Provide context parameters to generate a phishing landing page using
            AI
          </DialogDescription>
        </DialogHeader>

        <form
          onSubmit={handleSubmit(
            handleFormSubmit as SubmitHandler<TAILandingPageForm>,
          )}
          className="space-y-6"
        >
          {/* Basic Info */}
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <Label htmlFor="name">
                Page Name <span className="text-vibrant-red">*</span>
              </Label>
              <Input
                id="name"
                {...register('name')}
                placeholder="Enter landing page name"
                error={errors.name?.message}
              />
            </div>
            <div>
              <Label htmlFor="description">Description</Label>
              <Input
                id="description"
                {...register('description')}
                placeholder="Brief description of the page"
                error={errors.description?.message}
              />
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <Label htmlFor="category">
                Category <span className="text-vibrant-red">*</span>
              </Label>
              <SearchSelect
                key={`category-${isOpen ? 'open' : 'closed'}-${dropdowns.landingPageCategories.length}`}
                items={dropdowns.landingPageCategories.map(cat => ({
                  value: cat.id,
                  label: cat.name,
                }))}
                placeholder="Select category"
                value={watch('category')?.id}
                onValueChange={(value: string) =>
                  setValue(
                    'category',
                    pickIdName(dropdowns.landingPageCategories, value),
                    { shouldValidate: true },
                  )
                }
              />
              {errors.category && (
                <p className="mt-1 text-sm text-vibrant-red">
                  {idNameFieldError(errors.category)}
                </p>
              )}
            </div>
            <div>
              <Label htmlFor="difficultyLevel">
                Difficulty <span className="text-vibrant-red">*</span>
              </Label>
              <SearchSelect
                items={dropdowns.difficulties.map(level => ({
                  value: level.id,
                  label: level.name,
                }))}
                placeholder="Select difficulty level"
                value={watch('difficultyLevel')?.id}
                onValueChange={(value: string) =>
                  setValue(
                    'difficultyLevel',
                    pickIdName(dropdowns.difficulties, value),
                    { shouldValidate: true },
                  )
                }
              />
              {errors.difficultyLevel && (
                <p className="mt-1 text-sm text-vibrant-red">
                  {idNameFieldError(errors.difficultyLevel)}
                </p>
              )}
            </div>
          </div>

          <div>
            <Label htmlFor="trackingDomainId">
              Tracking Domain <span className="text-vibrant-red">*</span>
            </Label>
            <SearchSelect
              items={domainOptions.map(domain => ({
                value: domain.domainId,
                label: domain.domain,
              }))}
              placeholder="Select tracking domain"
              value={trackingDomainId}
              onValueChange={(value: string) =>
                setValue('trackingDomainId', value, { shouldValidate: true })
              }
            />
            {errors.trackingDomainId && (
              <p className="mt-1 text-sm text-vibrant-red">
                {errors.trackingDomainId.message}
              </p>
            )}
          </div>

          <div className="space-y-6">
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
            <div>
              <Label>Thumbnail</Label>
              <div className="flex w-full items-center gap-2 rounded-md border border-dashed border-card-border bg-transparent px-2 hover:bg-card-background">
                <input
                  type="file"
                  accept="image/png,image/jpeg"
                  id="aiLandingPageThumbnailUpload"
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
                      .getElementById('aiLandingPageThumbnailUpload')
                      ?.click()
                  }
                >
                  Choose file
                </Button>
              </div>
              {thumbnailPreviewUrl || thumbnailUrl ? (
                <div className="mt-3 flex items-center gap-6">
                  <img
                    src={thumbnailPreviewUrl || FILE_PATH_PREFIX + thumbnailUrl}
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
                JPG/PNG, max 2MB. Uploaded when you click Generate.
              </p>
            </div>
          </div>

          {/* AI Provider & Model */}
          <div className="rounded-lg border border-primary p-4">
            <div className="mb-4">
              {' '}
              <h4 className="font-medium text-primary">
                AI Generation Parameters
              </h4>
              <p className="text-sm text-muted-foreground">
                Select the AI provider and model to use for the landing page
                generation. You can also specify the tone and output language
                for the landing page.
              </p>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <Label htmlFor="providerType">
                  AI Provider <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={AI_GENERATE_PROVIDER_TYPES.map(p => ({
                    value: p,
                    label: p,
                  }))}
                  placeholder="Select provider"
                  value={watch('providerType')}
                  onValueChange={(value: string) =>
                    setValue(
                      'providerType',
                      value as (typeof AI_GENERATE_PROVIDER_TYPES)[number],
                      { shouldValidate: true },
                    )
                  }
                />
                {errors.providerType && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {errors.providerType.message}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="model">
                  Model <span className="text-vibrant-red">*</span>
                </Label>
                <Controller
                  name="model"
                  control={control}
                  render={({ field }) => (
                    <SearchSelect
                      key={`model-${isOpen ? 'open' : 'closed'}-${providerType}-${aiModels.length}`}
                      items={aiModels.map(m => ({
                        value: m.name,
                        label: m.name,
                      }))}
                      placeholder="Select model"
                      value={field.value || undefined}
                      onValueChange={value => field.onChange(value)}
                      disabled={modelsLoading || aiModels.length === 0}
                    />
                  )}
                />
                {errors.model && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {errors.model.message}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="generationOptions.tone">
                  Tone <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={dropdowns.tones.map(tone => ({
                    value: tone.id,
                    label: tone.name,
                  }))}
                  placeholder="Select tone"
                  value={watch('generationOptions.tone')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'generationOptions.tone',
                      pickIdName(dropdowns.tones, value),
                      { shouldValidate: true },
                    )
                  }
                />
                {errors.generationOptions?.tone && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.generationOptions.tone)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="targetDepartment">
                  Target Department <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={dropdowns.departments.map(dept => ({
                    value: dept.id,
                    label: dept.name,
                  }))}
                  placeholder="Select target department"
                  value={watch('targetDepartment')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'targetDepartment',
                      pickIdName(dropdowns.departments, value),
                      { shouldValidate: true },
                    )
                  }
                />
                {errors.targetDepartment && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.targetDepartment)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="urgencyLevel">
                  Urgency Level <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={dropdowns.urgencyLevels.map(level => ({
                    value: level.id,
                    label: level.name,
                  }))}
                  placeholder="Select urgency level"
                  value={watch('urgencyLevel')?.id}
                  onValueChange={(value: string) => {
                    const picked = pickIdName(dropdowns.urgencyLevels, value);
                    setValue('urgencyLevel', picked, { shouldValidate: true });
                    setValue('generationOptions.urgencyLevel', picked, {
                      shouldValidate: true,
                    });
                  }}
                />
                {errors.urgencyLevel && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.urgencyLevel)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="emotionalTrigger">
                  Emotional Trigger <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={dropdowns.emotionalTriggers.map(trigger => ({
                    value: trigger.id,
                    label: trigger.name,
                  }))}
                  placeholder="Select emotional trigger"
                  value={watch('emotionalTrigger')?.id}
                  onValueChange={(value: string) => {
                    const picked = pickIdName(
                      dropdowns.emotionalTriggers,
                      value,
                    );
                    setValue('emotionalTrigger', picked, {
                      shouldValidate: true,
                    });
                    setValue('generationOptions.emotionalTrigger', picked, {
                      shouldValidate: true,
                    });
                  }}
                />
                {errors.emotionalTrigger && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.emotionalTrigger)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="generationOptions.language">
                  Output Language
                </Label>
                <SearchSelect
                  items={languages.map(lang => ({
                    value: lang.displayName,
                    label: lang.displayName,
                  }))}
                  placeholder="Select output language"
                  value={watch('generationOptions.language') || undefined}
                  onValueChange={(value: string) =>
                    setValue('generationOptions.language', value, {
                      shouldValidate: true,
                    })
                  }
                  disabled={languagesLoading}
                />
                {errors.generationOptions?.language && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {errors.generationOptions.language.message}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="generationOptions.brand">Brand</Label>
                <SearchSelect
                  items={dropdowns.brands.map(brand => ({
                    value: brand.id,
                    label: brand.name,
                  }))}
                  placeholder="Select brand"
                  value={watch('generationOptions.brand')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'generationOptions.brand',
                      pickIdName(dropdowns.brands, value),
                      { shouldValidate: true },
                    )
                  }
                />
              </div>

              <div>
                <Label htmlFor="generationOptions.callToAction">
                  Call to Action
                </Label>
                <SearchSelect
                  items={dropdowns.callToActions.map(cta => ({
                    value: cta.id,
                    label: cta.name,
                  }))}
                  placeholder="Select call to action"
                  value={watch('generationOptions.callToAction')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'generationOptions.callToAction',
                      pickIdName(dropdowns.callToActions, value),
                      { shouldValidate: true },
                    )
                  }
                />
              </div>

              <div>
                <Label htmlFor="dataCaptureType">
                  Data Capture Type
                </Label>
                <SearchSelect
                  items={dropdowns.dataCaptureTypes.map(type => ({
                    value: type.id,
                    label: type.name,
                  }))}
                  placeholder="Select data capture type"
                  value={watch('dataCaptureType')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'dataCaptureType',
                      pickIdName(dropdowns.dataCaptureTypes, value),
                      { shouldValidate: true },
                    )
                  }
                />
              </div>

              {/* Generation Mode */}
              <div className="md:col-span-2">
                <Label>Generation Mode</Label>
                <Controller
                  name="generationMode"
                  control={control}
                  render={({ field }) => (
                    <div className="mt-2 grid grid-cols-3 gap-3">
                      {GENERATION_MODES.map(mode => (
                        <label
                          key={mode.id}
                          className={`flex cursor-pointer flex-col rounded-lg border p-3 transition-colors ${field.value === mode.id
                              ? 'border-primary bg-primary/10'
                              : 'hover:border-primary'
                            }`}
                        >
                          <input
                            type="radio"
                            {...field}
                            value={mode.id}
                            checked={field.value === mode.id}
                            className="sr-only"
                          />
                          <span className="text-sm font-medium">
                            {mode.label}
                          </span>
                          <span className="mt-1 text-xs text-gray-500">
                            {mode.description}
                          </span>
                        </label>
                      ))}
                    </div>
                  )}
                />
                {errors.generationMode && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {errors.generationMode.message}
                  </p>
                )}
              </div>

              {/* Layout Style */}
              {generationMode === 'FULLY_AI' && (
                <div className="md:col-span-2">
                  <Label>Layout Style</Label>
                  <Controller
                    name="layoutStyle"
                    control={control}
                    render={({ field }) => (
                      <div className="mt-2 grid grid-cols-2 gap-2 sm:grid-cols-4">
                        {LAYOUT_STYLES.map(style => (
                          <label
                            key={style.id}
                            className={`flex cursor-pointer flex-col items-center rounded-lg border p-2 transition-colors ${field.value === style.id
                                ? 'border-primary bg-primary/10'
                                : 'hover:border-primary'
                              }`}
                          >
                            <input
                              type="radio"
                              {...field}
                              value={style.id}
                              checked={field.value === style.id}
                              className="sr-only"
                            />
                            <span className="text-center text-xs font-medium">
                              {style.label}
                            </span>
                          </label>
                        ))}
                      </div>
                    )}
                  />
                  {errors.layoutStyle && (
                    <p className="mt-1 text-sm text-vibrant-red">
                      {errors.layoutStyle.message}
                    </p>
                  )}
                </div>
              )}
            </div>
          </div>

          <div className="rounded-lg border border-primary p-4">
            <div className="mb-4">
              <h4 className="font-medium text-primary">Input Source</h4>
              <RadioGroup
                value={inputMode}
                onValueChange={value =>
                  setInputMode(value as 'VOICE' | 'CONTEXT')
                }
                className="mt-2 flex gap-2"
              >
                {['VOICE', 'CONTEXT'].map(level => (
                  <div key={level} className="flex items-center space-x-2">
                    <RadioGroupItem value={level} id={level} />
                    <Label htmlFor={level} className="capitalize">
                      {level === 'VOICE' ? 'Voice Input' : 'Additional Context'}
                    </Label>
                  </div>
                ))}
              </RadioGroup>
            </div>

            {inputMode === 'VOICE' ? (
              <VoiceInputControl
                isRecording={isRecording}
                isProcessing={isProcessing}
                transcript={transcript}
                detectedLanguage={detectedLanguage}
                confidence={confidence}
                error={voiceError}
                selectedLanguage={selectedLanguage ?? ''}
                supportedLanguages={voiceSupportedLanguages}
                languagesLoading={languagesLoading}
                onLanguageChange={lang => setValue('inputLanguage', lang)}
                onStartRecording={handleStartRecording}
                onStopRecording={stopRecording}
                onClear={clearTranscript}
                onTranscriptChange={updateTranscript}
              />
            ) : (
              <div>
                <Label htmlFor="additionalContext">Additional Context</Label>
                <Textarea
                  id="additionalContext"
                  {...register('additionalContext')}
                  placeholder="Provide any additional context or requirements for the AI landing page..."
                  className="h-80"
                />
                {errors.additionalContext && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {errors.additionalContext.message}
                  </p>
                )}
              </div>
            )}

            <input type="hidden" {...register('voiceInput')} />
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={handleClose}>
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={
                !isValid || generating || uploadingThumbnail || isSubmitting
              }
              className="gap-2"
            >
              {generating || uploadingThumbnail ? (
                <>
                  <Loader2 className="size-4 animate-spin" />
                  {uploadingThumbnail
                    ? 'Uploading thumbnail...'
                    : 'Generating...'}
                </>
              ) : (
                <>
                  <Sparkles className="size-4" />
                  Generate Landing Page
                </>
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default AILandingPageModal;
