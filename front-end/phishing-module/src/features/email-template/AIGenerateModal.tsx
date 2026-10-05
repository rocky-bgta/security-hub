import { useState, useEffect, useCallback, useMemo } from 'react';
import { Controller, SubmitHandler, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Sparkles, Loader2, Upload, Trash } from 'lucide-react';
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
import CustomSelect from 'common/CustomSelect';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import VoiceInputControl from './VoiceInputControl';
import TagSelector from './TagSelector';
import EmployeeDataFieldSelector from './EmployeeDataFieldSelector';
import useVoiceRecording from 'hooks/UseVoiceRecording';
import { useLandingPages } from 'hooks/UseLandingPages';
import { useUploader } from 'hooks/UseUploader';
import {
  AI_GENERATE_PROVIDER_TYPES,
  EmployeeDataField,
  IIdName,
} from 'models/EmailTemplate';
import { CampaignChannel } from 'models/Campaign';
import { IAIModelItem } from 'models/AiProvider';
import {
  AIGenerateSchema,
  TAIGenerateForm,
  DefaultAIGenerateValues,
  emptyIdName,
} from 'schemas/EmailTemplateSchema';
import { Textarea } from 'common/Textarea';
import { useAPI } from 'hooks/UseAPI';
import { FileType, IActiveLanguage, IResponse } from 'models/Global';
import { ICustomSelectOption, TMultiValue } from 'models/Input';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import SearchSelect from 'components/SearchSelect';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import useDropDown from 'hooks/UseDropDown';
import { IDropdownItem } from 'models/DropDown';

const MAX_UPLOAD_SIZE_BYTES = 2 * 1024 * 1024;

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

interface AIGenerateModalProps {
  isOpen: boolean;
  onClose: () => void;
  onGenerate: (data: TAIGenerateForm) => Promise<void>;
  isGenerating: boolean;
}

/**
 * Modal for AI-powered email template generation
 * Based on Task-03: AI generation with voice input support
 */
const AIGenerateModal = ({
  isOpen,
  onClose,
  onGenerate,
  isGenerating,
}: AIGenerateModalProps) => {
  const { uploadFile } = useUploader();
  const apiClient = useAPI();
  const { fetchLandingPages, loading: landingPagesLoading, landingPages } =
    useLandingPages();

  const landingPageOptions = useMemo<ICustomSelectOption[]>(
    () =>
      landingPages.items.map(page => ({
        id: page.pageId,
        label: page.name,
        value: page.pageId,
      })),
    [landingPages.items],
  );

  const [inputMode, setInputMode] = useState<'VOICE' | 'CONTEXT'>('VOICE');
  const [targetingMode, setTargetingMode] = useState<'INDUSTRY' | 'DEPARTMENT'>(
    'INDUSTRY',
  );
  const [languages, setLanguages] = useState<IActiveLanguage[]>([]);
  const [languagesLoading, setLanguagesLoading] = useState(false);
  const [thumbnailFile, setThumbnailFile] = useState<File | null>(null);
  const [thumbnailPreviewUrl, setThumbnailPreviewUrl] = useState<string | null>(
    null,
  );
  const [thumbnailUploading, setThumbnailUploading] = useState(false);
  const [aiModels, setAiModels] = useState<IAIModelItem[]>([]);
  const [modelsLoading, setModelsLoading] = useState(false);
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
    fetchPayloadTypes,
    fetchTones,
    fetchAttackerPersonas,
    fetchSocialEngineeringStrategies,
    fetchAttackTechniques,
    fetchTriggerEvents,
    fetchExpectedUserActions,
    fetchCampaignObjectives,
    fetchIndustries,
    fetchDepartments,
    fetchConstraints,
    fetchDifficulties,
  } = useDropDown();

  const [dropdowns, setDropdowns] = useState<{
    payloadTypes: IDropdownItem[];
    tones: IDropdownItem[];
    attackerPersonas: IDropdownItem[];
    socialEngineeringStrategies: IDropdownItem[];
    attackTechniques: IDropdownItem[];
    triggerEvents: IDropdownItem[];
    expectedUserActions: IDropdownItem[];
    campaignObjectives: IDropdownItem[];
    industries: Array<{ id: string; name: string }>;
    departments: IDropdownItem[];
    constraints: IDropdownItem[];
    difficulties: IDropdownItem[];
  }>({
    payloadTypes: [],
    tones: [],
    attackerPersonas: [],
    socialEngineeringStrategies: [],
    attackTechniques: [],
    triggerEvents: [],
    expectedUserActions: [],
    campaignObjectives: [],
    industries: [],
    departments: [],
    constraints: [],
    difficulties: [],
  });

  const {
    register,
    control,
    handleSubmit,
    setValue,
    watch,
    reset,
    getValues,
    formState: { errors, isValid },
  } = useForm<TAIGenerateForm>({
    resolver: zodResolver(AIGenerateSchema),
    defaultValues: DefaultAIGenerateValues,
    mode: 'onChange',
  });

  const selectedLanguage = watch('inputLanguage');
  const providerType = watch('providerType');
  const isTargetIndustryDisabled = targetingMode === 'DEPARTMENT';
  const isDepartmentDisabled = targetingMode === 'INDUSTRY';

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
    void fetchLandingPages({
      offset: 0,
      pageSize: 100,
      status: 'ACTIVE',
      sortBy: 'name',
      sortOrder: 'asc',
    });
  }, [isOpen, fetchActiveLanguages, fetchLandingPages]);

  useEffect(() => {
    const loadPayloadTypes = async () => {
      const data = await fetchPayloadTypes(CampaignChannel.EMAIL);
      setDropdowns(prev => ({ ...prev, payloadTypes: data.items }));
    };
    const loadTones = async () => {
      const data = await fetchTones();
      setDropdowns(prev => ({ ...prev, tones: data.items }));
    };
    const loadAttackerPersonas = async () => {
      const data = await fetchAttackerPersonas();
      setDropdowns(prev => ({ ...prev, attackerPersonas: data.items }));
    };
    const loadSocialEngineeringStrategies = async () => {
      const data = await fetchSocialEngineeringStrategies();
      setDropdowns(prev => ({
        ...prev,
        socialEngineeringStrategies: data.items,
      }));
    };
    const loadAttackTechniques = async () => {
      const data = await fetchAttackTechniques();
      setDropdowns(prev => ({ ...prev, attackTechniques: data.items }));
    };
    const loadTriggerEvents = async () => {
      const data = await fetchTriggerEvents();
      setDropdowns(prev => ({ ...prev, triggerEvents: data.items }));
    };
    const loadExpectedUserActions = async () => {
      const data = await fetchExpectedUserActions();
      setDropdowns(prev => ({ ...prev, expectedUserActions: data.items }));
    };
    const loadCampaignObjectives = async () => {
      const data = await fetchCampaignObjectives();
      setDropdowns(prev => ({ ...prev, campaignObjectives: data.items }));
    };
    const loadIndustriesList = async () => {
      try {
        const response = await fetchIndustries();
        if (isSuccessResponse(response.statusCode)) {
          setDropdowns(prev => ({
            ...prev,
            industries: response.data ?? [],
          }));
        }
      } catch (e) {
        console.error('Error loading industries:', e);
      }
    };
    const loadDepartments = async () => {
      const data = await fetchDepartments();
      setDropdowns(prev => ({ ...prev, departments: data.items }));
    };
    const loadConstraints = async () => {
      const data = await fetchConstraints();
      setDropdowns(prev => ({ ...prev, constraints: data.items }));
    };
    const loadDifficulties = async () => {
      const data = await fetchDifficulties();
      setDropdowns(prev => ({ ...prev, difficulties: data.items }));
    };

    if (isOpen) {
      void loadPayloadTypes();
      void loadTones();
      void loadAttackerPersonas();
      void loadSocialEngineeringStrategies();
      void loadAttackTechniques();
      void loadTriggerEvents();
      void loadExpectedUserActions();
      void loadCampaignObjectives();
      void loadIndustriesList();
      void loadDepartments();
      void loadConstraints();
      void loadDifficulties();
    }
  }, [
    isOpen,
    fetchPayloadTypes,
    fetchTones,
    fetchAttackerPersonas,
    fetchSocialEngineeringStrategies,
    fetchAttackTechniques,
    fetchTriggerEvents,
    fetchExpectedUserActions,
    fetchCampaignObjectives,
    fetchIndustries,
    fetchDepartments,
    fetchConstraints,
    fetchDifficulties,
  ]);

  useEffect(() => {
    if (!isOpen) return;
    void fetchAiModels(providerType);
  }, [isOpen, providerType, fetchAiModels]);

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
    if (!isOpen || dropdowns.payloadTypes.length === 0) return;
    const current = getValues('payloadType');
    if (current?.id && dropdowns.payloadTypes.some(p => p.id === current.id)) {
      return;
    }
    const defaultItem =
      dropdowns.payloadTypes.find(p => p.isDefault) ??
      dropdowns.payloadTypes[0];
    if (defaultItem) {
      setValue(
        'payloadType',
        { id: defaultItem.id, name: defaultItem.name },
        { shouldValidate: true },
      );
    }
  }, [isOpen, dropdowns.payloadTypes, getValues, setValue]);

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
    setValue('generationMode', inputMode, { shouldValidate: true });
  }, [inputMode, setValue]);

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

  // Update voice input content when transcript changes
  useEffect(() => {
    if (transcript) {
      setValue('voiceInputContent', transcript, { shouldValidate: true });
      if (detectedLanguage) {
        setValue('inputLanguage', detectedLanguage, { shouldValidate: true });
      }
    }
  }, [transcript, detectedLanguage, setValue]);

  // Reset form when modal closes
  useEffect(() => {
    if (!isOpen) {
      reset(DefaultAIGenerateValues);
      clearTranscript();
      setInputMode('VOICE');
      setTargetingMode('INDUSTRY');
      setThumbnailFile(null);
    }
  }, [isOpen, reset, clearTranscript]);

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

  const handleFormSubmit = async (data: TAIGenerateForm) => {
    let thumbnailUrlFinal =
      typeof data.thumbnailUrl === 'string' ? data.thumbnailUrl : '';
    if (thumbnailFile) {
      setThumbnailUploading(true);
      try {
        const { url, error } = await uploadFile(
          thumbnailFile,
          FileType.THUMBNAIL,
        );
        if (error || !url) {
          toast.error(error || 'Failed to upload thumbnail. Please try again.');
          return;
        }
        thumbnailUrlFinal = url;
      } finally {
        setThumbnailUploading(false);
      }
    }
    await onGenerate({
      ...data,
      thumbnailUrl: thumbnailUrlFinal,
      generationMode: inputMode,
      targetIndustry:
        targetingMode === 'INDUSTRY' ? data.targetIndustry : emptyIdName,
      department:
        targetingMode === 'DEPARTMENT' ? data.department : emptyIdName,
    });
  };

  const handleStartRecording = () => {
    startRecording(selectedLanguage || undefined);
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] w-4/5 overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Sparkles className="size-5 text-primary" />
            Generate Email Template with AI
          </DialogTitle>
          <DialogDescription>
            Provide context parameters to generate a phishing email template
            using AI
          </DialogDescription>
        </DialogHeader>

        <form
          onSubmit={handleSubmit(
            handleFormSubmit as SubmitHandler<TAIGenerateForm>,
          )}
          className="space-y-6"
        >
          {/* Basic Info */}
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <Label htmlFor="templateName">
                Template Name <span className="text-vibrant-red">*</span>
              </Label>
              <Input
                id="templateName"
                {...register('templateName')}
                placeholder="Enter template name"
                error={errors.templateName?.message}
              />
            </div>
            <div>
              <Label htmlFor="emailSubject">
                Email Subject <span className="text-vibrant-red">*</span>
              </Label>
              <Input
                id="emailSubject"
                {...register('emailSubject')}
                placeholder="Enter email subject line"
                error={errors.emailSubject?.message}
              />
            </div>
          </div>

          {/* AI Context Parameters */}
          <div className="rounded-lg border border-primary p-4">
            <div className="mb-4">
              {' '}
              <h4 className="font-medium text-primary">
                AI Generation Parameters
              </h4>
              <p className="text-sm text-muted-foreground">
                Select the AI provider and model to use for the email template
                generation. You can also specify the tone and output language
                for the email template.
              </p>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <Label htmlFor="providerType">
                  AI provider <span className="text-vibrant-red">*</span>
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
                  placeholder="Select an option"
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
                <Label htmlFor="attackerPersona">
                  Attacker Persona <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={dropdowns.attackerPersonas.map(persona => ({
                    value: persona.id,
                    label: persona.name,
                  }))}
                  placeholder="Select an option"
                  value={watch('attackerPersona')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'attackerPersona',
                      pickIdName(dropdowns.attackerPersonas, value),
                      { shouldValidate: true },
                    )
                  }
                />
                {errors.attackerPersona && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.attackerPersona)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="socialEngineeringStrategy">
                  Social Engineering Strategy{' '}
                  <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={dropdowns.socialEngineeringStrategies.map(
                    strategy => ({
                      value: strategy.id,
                      label: strategy.name,
                    }),
                  )}
                  placeholder="Select an option"
                  value={watch('socialEngineeringStrategy')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'socialEngineeringStrategy',
                      pickIdName(
                        dropdowns.socialEngineeringStrategies,
                        value,
                      ),
                      { shouldValidate: true },
                    )
                  }
                />
                {errors.socialEngineeringStrategy && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.socialEngineeringStrategy)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="attackTechnique">
                  Attack Technique <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={dropdowns.attackTechniques.map(technique => ({
                    value: technique.id,
                    label: technique.name,
                  }))}
                  placeholder="Select an option"
                  value={watch('attackTechnique')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'attackTechnique',
                      pickIdName(dropdowns.attackTechniques, value),
                      { shouldValidate: true },
                    )
                  }
                />
                {errors.attackTechnique && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.attackTechnique)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="triggerEvent">
                  Trigger Event <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={dropdowns.triggerEvents.map(event => ({
                    value: event.id,
                    label: event.name,
                  }))}
                  placeholder="Select an option"
                  value={watch('triggerEvent')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'triggerEvent',
                      pickIdName(dropdowns.triggerEvents, value),
                      { shouldValidate: true },
                    )
                  }
                />
                {errors.triggerEvent && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.triggerEvent)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="expectedUserAction">
                  Expected User Action{' '}
                  <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={dropdowns.expectedUserActions.map(action => ({
                    value: action.id,
                    label: action.name,
                  }))}
                  placeholder="Select an option"
                  value={watch('expectedUserAction')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'expectedUserAction',
                      pickIdName(dropdowns.expectedUserActions, value),
                      { shouldValidate: true },
                    )
                  }
                />
                {errors.expectedUserAction && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.expectedUserAction)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="campaignObjective">
                  Campaign Objective <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={dropdowns.campaignObjectives.map(objective => ({
                    value: objective.id,
                    label: objective.name,
                  }))}
                  placeholder="Select an option"
                  value={watch('campaignObjective')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'campaignObjective',
                      pickIdName(dropdowns.campaignObjectives, value),
                      { shouldValidate: true },
                    )
                  }
                />
                {errors.campaignObjective && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.campaignObjective)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="payloadType">
                  Payload Type <span className="text-vibrant-red">*</span>
                </Label>
                <Controller
                  name="payloadType"
                  control={control}
                  render={({ field }) => (
                    <SearchSelect
                      key={`payloadType-${isOpen ? 'open' : 'closed'}-${dropdowns.payloadTypes.length}`}
                      items={dropdowns.payloadTypes.map(type => ({
                        value: type.id,
                        label: type.name,
                      }))}
                      placeholder="Select payload type"
                      value={field.value?.id}
                      onValueChange={value =>
                        field.onChange(pickIdName(dropdowns.payloadTypes, value))
                      }
                    />
                  )}
                />
                {errors.payloadType && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.payloadType)}
                  </p>
                )}
              </div>

              <div className="md:col-span-2">
                <Label>Targeting Option</Label>
                <div className="mt-2 flex flex-wrap gap-4">
                  <RadioGroup
                    value={targetingMode}
                    onValueChange={value =>
                      setTargetingMode(value as 'INDUSTRY' | 'DEPARTMENT')
                    }
                    className="flex gap-2"
                  >
                    {['INDUSTRY', 'DEPARTMENT'].map(level => (
                      <div key={level} className="flex items-center space-x-2">
                        <RadioGroupItem value={level} id={level} />
                        <Label htmlFor={level} className="capitalize">
                          {level === 'INDUSTRY'
                            ? 'Target Industry'
                            : 'Department'}
                        </Label>
                      </div>
                    ))}
                  </RadioGroup>
                </div>
                <p className="mt-1 text-xs text-muted-foreground">
                  You can select either Target Industry or Department, not both.
                </p>
              </div>

              <div>
                <Label htmlFor="targetIndustry">
                  Target Industry{' '}
                  {isDepartmentDisabled ? (
                    <span className="text-vibrant-red">*</span>
                  ) : (
                    ''
                  )}
                </Label>
                <SearchSelect
                  items={dropdowns.industries.map(industry => ({
                    value: industry.id,
                    label: industry.name,
                  }))}
                  placeholder="Select an option"
                  value={watch('targetIndustry')?.id}
                  onValueChange={(value: string) => {
                    setValue(
                      'targetIndustry',
                      pickIdName(dropdowns.industries, value),
                      { shouldValidate: true },
                    );
                    if (value) {
                      setValue('department', emptyIdName, {
                        shouldValidate: true,
                      });
                    }
                  }}
                  disabled={isTargetIndustryDisabled}
                />
                {errors.targetIndustry && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.targetIndustry)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="department">
                  Department{' '}
                  {isTargetIndustryDisabled ? (
                    <span className="text-vibrant-red">*</span>
                  ) : (
                    ''
                  )}
                </Label>
                <SearchSelect
                  items={dropdowns.departments.map(department => ({
                    value: department.id,
                    label: department.name,
                  }))}
                  placeholder="Select an option"
                  value={watch('department')?.id}
                  onValueChange={(value: string) => {
                    setValue(
                      'department',
                      pickIdName(dropdowns.departments, value),
                      { shouldValidate: true },
                    );
                    if (value) {
                      setValue('targetIndustry', emptyIdName, {
                        shouldValidate: true,
                      });
                    }
                  }}
                  disabled={isDepartmentDisabled}
                />
                {errors.department && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.department)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="generationOptions.language">
                  Output language
                </Label>
                <SearchSelect
                  items={languages.map(lang => ({
                    value: lang.displayName,
                    label: lang.displayName,
                  }))}
                  placeholder="Select language"
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
                <Label htmlFor="generationOptions.constraints">
                  Constraints
                </Label>
                <SearchSelect
                  items={dropdowns.constraints.map(constraint => ({
                    value: constraint.id,
                    label: constraint.name,
                  }))}
                  placeholder="Select an option"
                  value={watch('generationOptions.constraints')?.id}
                  onValueChange={(value: string) =>
                    setValue(
                      'generationOptions.constraints',
                      pickIdName(dropdowns.constraints, value),
                      { shouldValidate: true },
                    )
                  }
                />
                {errors.generationOptions?.constraints && (
                  <p className="mt-1 text-sm text-vibrant-red">
                    {idNameFieldError(errors.generationOptions.constraints)}
                  </p>
                )}
              </div>

              <div>
                <Label htmlFor="difficultyLevel">
                  Difficulty Level <span className="text-vibrant-red">*</span>
                </Label>
                <SearchSelect
                  items={dropdowns.difficulties.map(level => ({
                    value: level.id,
                    label: level.name,
                  }))}
                  placeholder="Select an option"
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

              <div className="md:col-span-2">
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
            </div>
          </div>

          {/* Tags & Thumbnail — same pattern as EmailTemplateCreate */}
          <div className="space-y-6">
            <div>
              <Label>Thumbnail</Label>
              <div className="mt-2 flex flex-col gap-3 md:flex-row md:items-center">
                <div className="flex w-full items-center gap-2 rounded-md border border-dashed border-card-border bg-transparent hover:bg-card-background">
                  <input
                    type="file"
                    accept="image/png,image/jpeg"
                    id="aiGenerateThumbnailUpload"
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
                        .getElementById('aiGenerateThumbnailUpload')
                        ?.click()
                    }
                  >
                    Choose file
                    <Upload className="size-4" />
                  </Button>
                </div>
              </div>
              {errors.thumbnailUrl && (
                <p className="mt-1 text-sm text-vibrant-red">
                  {errors.thumbnailUrl.message}
                </p>
              )}
              {thumbnailPreviewUrl || watch('thumbnailUrl') ? (
                <div className="mt-3 flex items-center gap-6">
                  <img
                    src={
                      thumbnailPreviewUrl ||
                      FILE_PATH_PREFIX + (watch('thumbnailUrl') || '') ||
                      ''
                    }
                    alt="Thumbnail preview"
                    className="h-20 w-32 rounded-md border border-card-border object-cover"
                  />
                  {thumbnailFile || watch('thumbnailUrl') ? (
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

            <div>
              <Controller
                name="employeeDataRequired"
                control={control}
                render={({ field }) => (
                  <EmployeeDataFieldSelector
                    value={field.value as EmployeeDataField[]}
                    onChange={field.onChange}
                    error={errors.employeeDataRequired?.message}
                    className="mt-2"
                  />
                )}
              />
            </div>
          </div>

          {/* Input Mode Selection */}
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
                  className="h-80"
                  placeholder="Provide any additional context or requirements for the AI..."
                />
                <div className="flex items-center justify-between">
                  {errors.additionalContext && (
                    <p className="mt-1 text-sm text-vibrant-red">
                      {errors.additionalContext.message}
                    </p>
                  )}
                </div>
              </div>
            )}

            <input type="hidden" {...register('voiceInputContent')} />
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={!isValid || isGenerating || thumbnailUploading}
              className="gap-2"
            >
              {thumbnailUploading ? (
                <>
                  <Loader2 className="size-4 animate-spin" />
                  Uploading thumbnail...
                </>
              ) : isGenerating ? (
                <>
                  <Loader2 className="size-4 animate-spin" />
                  Generating...
                </>
              ) : (
                <>
                  <Sparkles className="size-4" />
                  Generate Template
                </>
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default AIGenerateModal;
