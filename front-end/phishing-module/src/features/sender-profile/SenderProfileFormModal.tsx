import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from 'common/Button';
import { Dialog, DialogContent, DialogTitle } from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import useDropDown from 'hooks/UseDropDown';
import { useAPI } from 'hooks/UseAPI';
import { InfoIcon } from 'lucide-react';
import { IDomainListResponse } from 'models/Domain';
import { IDropdownItem } from 'models/DropDown';
import { IIdName, toIdName } from 'models/EmailTemplate';
import {
  COMMON_SMTP_PORTS,
  DomainType,
  ISenderProfile,
  ISenderProfileForm,
  ITestResult,
  InterfaceType,
  ProfileType,
  ProviderType,
} from 'models/SenderProfile';
import { useEffect, useState } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  DISPLAY_NAME_MAX_ERROR_MESSAGE,
  DISPLAY_NAME_MAX_LENGTH,
  PROFILE_NAME_MAX_ERROR_MESSAGE,
  PROFILE_NAME_MAX_LENGTH,
  SenderProfileSchema,
  SenderProfileUpdateSchema,
  TSenderProfileForm,
  TSenderProfileUpdateForm,
  emptyIdName,
  senderProfileDefaultValues,
} from 'schemas/SenderProfileSchema';
import DomainWarning from './DomainWarning';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAuth } from 'hooks/UseAuth';
import { IResponse } from 'models/Global';
import { ROLE } from 'utils/Role';

const PSYCHOLOGICAL_TRIGGERS = ['Authority', 'Urgency', 'Curiosity', 'Fear'];
const TAG_SUGGESTIONS = [
  'Security',
  'Awareness',
  'Phishing',
  'Training',
  'Compliance',
  'GDPR',
];
const MAX_TAGS = 10;

const normalizeOptionalString = (value?: string) => {
  const trimmedValue = value?.trim();
  return trimmedValue ? trimmedValue : undefined;
};

const normalizeOptionalEnum = <T extends string>(value?: T | '') =>
  value ? value : undefined;

const normalizeOptionalIdName = (value?: IIdName) => {
  const id = value?.id?.trim();
  if (!id) {
    return undefined;
  }
  return { id, name: value?.name?.trim() || id };
};

const normalizeOptionalArray = (value?: string[]) =>
  value && value.length > 0 ? value : undefined;

const pickIdName = (
  items: Array<{ id: string; name: string }>,
  id: string,
): IIdName => {
  const item = items.find(i => i.id === id);
  return item ? { id: item.id, name: item.name } : emptyIdName;
};

const buildSenderProfilePayload = (
  data: TSenderProfileForm | TSenderProfileUpdateForm,
): ISenderProfileForm => ({
  profileName: data.profileName.trim(),
  interfaceType: data.interfaceType,
  fromAddress: data.fromAddress.trim(),
  displayName: data.displayName.trim(),
  replyToAddress: normalizeOptionalString(data.replyToAddress),
  host: data.host.trim(),
  port: data.port,
  username: data.username.trim(),
  password: data.password,
  category: normalizeOptionalString(data.category),
  targetIndustryId: normalizeOptionalString(data.targetIndustryId),
  regionId: normalizeOptionalString(data.regionId),
  language: normalizeOptionalString(data.language),
  deceptionLevel: normalizeOptionalIdName(data.deceptionLevel),
  psychologicalTriggers: normalizeOptionalArray(data.psychologicalTriggers),
  domainType: normalizeOptionalEnum(data.domainType),
  domainName: normalizeOptionalString(data.domainName),
  personalizationLevel: normalizeOptionalIdName(data.personalizationLevel),
  providerType: normalizeOptionalEnum(data.providerType),
  tags: normalizeOptionalArray(data.tags),
  ignoreCertificateErrors: data.ignoreCertificateErrors,
  useTls: data.useTls,
});

interface SenderProfileFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  profile?: ISenderProfile | null;
  onSave: (data: ISenderProfileForm) => Promise<ISenderProfile | null>;
  onTest: (data: ISenderProfileForm) => Promise<ITestResult | null>;
  onCheckDomain: (email: string) => Promise<boolean>;
  saving?: boolean;
  testing?: boolean;
}

interface IDropdownOption {
  id: string;
  name: string;
}

interface ICountry extends IDropdownOption {
  code: string;
}

type IIndustry = IDropdownOption;

export const SenderProfileFormModal = ({
  isOpen,
  onClose,
  profile,
  onSave,
  onTest,
  onCheckDomain,
  saving = false,
  testing = false,
}: SenderProfileFormModalProps) => {
  const isEditing = !!profile;
  const isManaged = profile?.profileType === ProfileType.MANAGED;

  const [testResult, setTestResult] = useState<ITestResult | null>(null);
  const [domainWarning, setDomainWarning] = useState(false);
  const [industryOptions, setIndustryOptions] = useState<
    { id: string; name: string }[]
  >([]);
  const [regionOptions, setRegionOptions] = useState<
    { id: string; name: string; code: string }[]
  >([]);
  const [domainOptions, setDomainOptions] = useState<string[]>([]);
  const [personalizationLevelOptions, setPersonalizationLevelOptions] = useState<
    IDropdownItem[]
  >([]);
  const [deceptionLevelOptions, setDeceptionLevelOptions] = useState<
    IDropdownItem[]
  >([]);
  const [tagInput, setTagInput] = useState('');

  const apiClient = useAPI();
  const { fetchPersonalizationLevels, fetchDeceptionLevels } = useDropDown();
  const { role } = useAuth();

  const {
    register,
    control,
    getValues,
    setValue,
    handleSubmit,
    reset,
    formState,
  } = useForm<TSenderProfileForm | TSenderProfileUpdateForm>({
    resolver: zodResolver(
      isEditing ? SenderProfileUpdateSchema : SenderProfileSchema,
      undefined,
      { mode: 'sync' },
    ),
    mode: 'onChange',
    reValidateMode: 'onChange',
    defaultValues: senderProfileDefaultValues,
  });

  const { errors } = formState;
  const fromAddress = useWatch({ control, name: 'fromAddress' }) ?? '';
  const selectedDomainName = useWatch({ control, name: 'domainName' }) ?? '';
  const selectedProviderType =
    useWatch({ control, name: 'providerType' }) ?? '';
  const profileNameValue = useWatch({ control, name: 'profileName' }) ?? '';
  const displayNameValue = useWatch({ control, name: 'displayName' }) ?? '';

  const showProfileNameMaxMessage =
    !isManaged &&
    !errors.profileName &&
    profileNameValue.length >= PROFILE_NAME_MAX_LENGTH;
  const showDisplayNameMaxMessage =
    !isManaged &&
    !errors.displayName &&
    displayNameValue.length >= DISPLAY_NAME_MAX_LENGTH;
  const shouldShowProviderType = role !== ROLE.CLIENT_ADMIN;
  const shouldShowCredentials =
    role === ROLE.CLIENT_ADMIN ||
    (role !== ROLE.CLIENT_ADMIN && selectedProviderType === ProviderType.OTHER);

  useEffect(() => {
    const fetchDropdownOptions = async () => {
      try {
        const [
          countryRes,
          industryRes,
          domainRes,
          personalizationRes,
          deceptionRes,
        ] = await Promise.all([
          apiClient.get(API_END_POINTS.GET_ACTIVE_COUNTRY_LIST) as Promise<
            IResponse<Array<ICountry>>
          >,
          apiClient.get(API_END_POINTS.GET_ACTIVE_INDUSTRIES_LIST) as Promise<
            IResponse<Array<IIndustry>>
          >,
          apiClient.get(
            `${API_END_POINTS.DOMAIN_LIST}offset=0&pageSize=1000`,
          ) as Promise<IResponse<IDomainListResponse>>,
          fetchPersonalizationLevels(),
          fetchDeceptionLevels(),
        ]);

        if (countryRes)
          setRegionOptions(
            countryRes.data.map(item => ({
              id: item.id,
              name: item.name,
              code: item.code,
            })),
          );
        if (industryRes)
          setIndustryOptions(
            industryRes.data.map(item => ({
              id: item.id,
              name: item.name,
            })),
          );
        if (domainRes) {
          setDomainOptions(domainRes.data.items.map(item => item.domain));
        }
        setPersonalizationLevelOptions(personalizationRes.items);
        setDeceptionLevelOptions(deceptionRes.items);
      } catch (error) {
        console.error('Error fetching dropdown data:', error);
      }
    };
    fetchDropdownOptions();
  }, [apiClient, fetchDeceptionLevels, fetchPersonalizationLevels]);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    if (profile) {
      reset({
        profileName: profile.profileName,
        interfaceType: profile.interfaceType,
        fromAddress: profile.fromAddress,
        displayName: profile.displayName || '',
        replyToAddress: profile.replyToAddress || '',
        host: profile.host,
        port: profile.port,
        username: profile.username,
        password: '',
        category: profile.category || '',
        targetIndustryId: profile.targetIndustryId || '',
        regionId: profile.regionId || '',
        language: profile.language || '',
        deceptionLevel: toIdName(
          profile.deceptionLevel,
          deceptionLevelOptions,
        ),
        psychologicalTriggers: profile.psychologicalTriggers || [],
        domainType: profile.domainType || '',
        domainName: profile.domainName || '',
        personalizationLevel: toIdName(
          profile.personalizationLevel,
          personalizationLevelOptions,
        ),
        providerType: profile.providerType || '',
        tags: profile.tags || [],
        ignoreCertificateErrors: profile.ignoreCertificateErrors,
        useTls: profile.useTls,
      });
      return;
    }

    reset(senderProfileDefaultValues);
  }, [
    isOpen,
    profile,
    reset,
    deceptionLevelOptions,
    personalizationLevelOptions,
  ]);

  useEffect(() => {
    const checkDomain = async () => {
      if (fromAddress && fromAddress.includes('@')) {
        const isVerified = await onCheckDomain(fromAddress);
        setDomainWarning(!isVerified);
        return;
      }

      setDomainWarning(false);
    };

    const timer = setTimeout(checkDomain, 500);
    return () => clearTimeout(timer);
  }, [fromAddress, onCheckDomain]);

  const handleClose = () => {
    setTestResult(null);
    setDomainWarning(false);
    setTagInput('');
    onClose();
  };

  const handleTest = async () => {
    const data = getValues();

    if (!data.host || !data.port || !data.username || !data.password) {
      return;
    }

    setTestResult(null);
    const result = await onTest(buildSenderProfilePayload(data));
    setTestResult(result);
  };

  const onSubmit = async (
    data: TSenderProfileForm | TSenderProfileUpdateForm,
  ) => {
    const result = await onSave(buildSenderProfilePayload(data));
    if (result) {
      handleClose();
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={open => !open && handleClose()}>
      <DialogContent>
        <DialogTitle>
          {isEditing ? 'Edit Sender Profile' : 'Create Sender Profile'}
        </DialogTitle>
        <form
          onSubmit={handleSubmit(onSubmit)}
          className="max-h-[85vh] space-y-6 overflow-auto px-1"
        >
          {isManaged && (
            <div className="rounded-lg border border-card-border bg-card p-4">
              <div className="flex items-center gap-2 text-card-foreground">
                <InfoIcon className="size-5" />
                <span className="font-medium">
                  This is a managed profile (read-only)
                </span>
              </div>
            </div>
          )}

          {domainWarning && !isManaged && (
            <DomainWarning
              email={fromAddress}
              onDismiss={() => setDomainWarning(false)}
            />
          )}

          <h4 className="mb-4 text-sm font-medium">Basic Information</h4>
          <div className="grid grid-cols-3 gap-4">
            <div className="space-y-1">
              <Label htmlFor="profileName">Profile Name</Label>
              <Input
                id="profileName"
                {...register('profileName')}
                maxLength={PROFILE_NAME_MAX_LENGTH}
                placeholder="My SMTP Profile"
                disabled={isManaged}
                className={
                  errors.profileName || showProfileNameMaxMessage
                    ? 'border-red-500'
                    : ''
                }
              />
              {errors.profileName && (
                <p className="mt-1 text-sm text-red-500">
                  {errors.profileName.message}
                </p>
              )}
              {showProfileNameMaxMessage && (
                <p className="mt-1 text-sm text-red-500">
                  {PROFILE_NAME_MAX_ERROR_MESSAGE}
                </p>
              )}
            </div>

            <div className="space-y-1">
              <Label htmlFor="category">Category</Label>
              <Controller
                name="category"
                control={control}
                render={({ field }) => (
                  <Select
                    value={field.value}
                    onValueChange={field.onChange}
                    disabled={isManaged}
                  >
                    <SelectTrigger id="category">
                      <SelectValue placeholder="Select category" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Internal">Internal</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>

            <div className="space-y-1">
              <Label htmlFor="interfaceType">Interface Type</Label>
              <Controller
                name="interfaceType"
                control={control}
                render={({ field }) => (
                  <Select
                    value={field.value}
                    onValueChange={field.onChange}
                    disabled={isManaged}
                  >
                    <SelectTrigger id="interfaceType">
                      <SelectValue placeholder="Select interface type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={InterfaceType.SMTP}>SMTP</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>
          </div>

          <div className="border-t border-gray-200 pt-4">
            <h4 className="mb-4 text-sm font-medium">Targeting & Context</h4>

            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-1">
                <Label htmlFor="targetIndustryId">Target Industry</Label>
                <Controller
                  name="targetIndustryId"
                  control={control}
                  render={({ field }) => (
                    <Select
                      value={field.value}
                      onValueChange={field.onChange}
                      disabled={isManaged}
                    >
                      <SelectTrigger id="targetIndustryId">
                        <SelectValue placeholder="Select industry" />
                      </SelectTrigger>
                      <SelectContent>
                        {industryOptions.map(option => (
                          <SelectItem key={option.id} value={option.id}>
                            {option.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>

              <div className="space-y-1">
                <Label htmlFor="regionId">Region</Label>
                <Controller
                  name="regionId"
                  control={control}
                  render={({ field }) => (
                    <Select
                      value={field.value}
                      onValueChange={field.onChange}
                      disabled={isManaged}
                    >
                      <SelectTrigger id="regionId">
                        <SelectValue placeholder="Select region" />
                      </SelectTrigger>
                      <SelectContent>
                        {regionOptions.map(option => (
                          <SelectItem key={option.id} value={option.id}>
                            {option.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>

              <div className="space-y-1">
                <Label htmlFor="personalizationLevel">
                  Personalization Level
                </Label>
                <Controller
                  name="personalizationLevel"
                  control={control}
                  render={({ field }) => (
                    <Select
                      value={field.value?.id || undefined}
                      onValueChange={value =>
                        field.onChange(
                          pickIdName(personalizationLevelOptions, value),
                        )
                      }
                      disabled={isManaged}
                    >
                      <SelectTrigger id="personalizationLevel">
                        <SelectValue placeholder="Select personalization level" />
                      </SelectTrigger>
                      <SelectContent>
                        {personalizationLevelOptions.map(option => (
                          <SelectItem key={option.id} value={option.id}>
                            {option.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
            </div>
          </div>

          <div className="border-t border-gray-200 pt-4">
            <h4 className="mb-4 text-sm font-medium">Attack Intelligence</h4>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <Label htmlFor="deceptionLevel">Deception Level</Label>
                <Controller
                  name="deceptionLevel"
                  control={control}
                  render={({ field }) => (
                    <Select
                      value={field.value?.id || undefined}
                      onValueChange={value =>
                        field.onChange(pickIdName(deceptionLevelOptions, value))
                      }
                      disabled={isManaged}
                    >
                      <SelectTrigger id="deceptionLevel">
                        <SelectValue placeholder="Select deception level" />
                      </SelectTrigger>
                      <SelectContent>
                        {deceptionLevelOptions.map(option => (
                          <SelectItem key={option.id} value={option.id}>
                            {option.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>

              <div className="space-y-1">
                <Label>Psychological Triggers</Label>
                <Controller
                  name="psychologicalTriggers"
                  control={control}
                  render={({ field }) => (
                    <div className="grid grid-cols-4 gap-x-4 gap-y-2 pt-1">
                      {PSYCHOLOGICAL_TRIGGERS.map(trigger => (
                        <Label
                          key={trigger}
                          className="flex cursor-pointer items-center gap-2 rounded-md p-2 text-sm"
                        >
                          <input
                            type="checkbox"
                            value={trigger}
                            checked={(field.value ?? []).includes(trigger)}
                            disabled={isManaged}
                            onChange={e => {
                              const current = field.value ?? [];
                              field.onChange(
                                e.target.checked
                                  ? [...current, trigger]
                                  : current.filter(v => v !== trigger),
                              );
                            }}
                            className="size-4 rounded border-card-border text-primary focus:ring-primary"
                          />
                          <span className="text-sm">{trigger}</span>
                        </Label>
                      ))}
                    </div>
                  )}
                />
              </div>
            </div>
          </div>

          <div className="border-t border-gray-200 pt-4">
            <h4 className="mb-4 text-sm font-medium">
              Domain and Email Configuration
            </h4>

            <div className="mb-4 space-y-1">
              <Label htmlFor="domainType">Domain Type</Label>
              <Controller
                name="domainType"
                control={control}
                render={({ field }) => (
                  <div className="grid grid-cols-2 gap-x-4 gap-y-2 pt-1">
                    {[
                      {
                        title: DomainType.LOOKALIKE_DOMAIN,
                        description: 'Slightly modified to appear legitimate',
                      },
                      {
                        title: DomainType.SPOOFED_DOMAIN,
                        description: 'Uses your internal domain',
                      },
                    ].map(option => (
                      <label
                        key={option.title}
                        className="flex cursor-pointer items-center gap-2"
                      >
                        <input
                          type="radio"
                          name="domainType"
                          value={option.title}
                          checked={field.value === option.title}
                          disabled={isManaged}
                          onChange={() => field.onChange(option.title)}
                          className="size-4 border-card-border text-primary focus:ring-primary"
                        />
                        <span className="flex flex-col text-sm capitalize">
                          {option.title.toLowerCase().replace('_', ' ')}
                          <span className="text-[10px] text-gray-400">
                            {option.description}
                          </span>
                        </span>
                      </label>
                    ))}
                  </div>
                )}
              />
            </div>

            <div className="mb-4 grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <Label htmlFor="domainName">Domain Name</Label>
                <Controller
                  name="domainName"
                  control={control}
                  render={({ field }) => (
                    <Select
                      value={field.value}
                      onValueChange={value => {
                        const currentFromAddress =
                          getValues('fromAddress') ?? '';
                        const currentLocalPart = currentFromAddress.includes(
                          '@',
                        )
                          ? currentFromAddress.split('@')[0]
                          : currentFromAddress;

                        field.onChange(value);

                        if (currentLocalPart.trim()) {
                          setValue(
                            'fromAddress',
                            `${currentLocalPart.trim()}@${value}`,
                            {
                              shouldDirty: true,
                              shouldValidate: true,
                            },
                          );
                        } else {
                          setValue('fromAddress', '', {
                            shouldDirty: true,
                            shouldValidate: true,
                          });
                        }
                      }}
                      disabled={isManaged}
                    >
                      <SelectTrigger id="domainName">
                        <SelectValue placeholder="Select domain name" />
                      </SelectTrigger>
                      <SelectContent>
                        {domainOptions.map(domain => (
                          <SelectItem key={domain} value={domain}>
                            {domain}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>

              <div className="space-y-1">
                <Label htmlFor="fromAddress">Email Address</Label>
                <Controller
                  name="fromAddress"
                  control={control}
                  render={({ field }) => {
                    const localPart = field.value?.includes('@')
                      ? field.value.split('@')[0]
                      : (field.value ?? '');

                    return (
                      <div className="flex items-center gap-2">
                        <Input
                          id="fromAddress"
                          type="text"
                          value={localPart}
                          onChange={event => {
                            const nextLocalPart = event.target.value.trim();
                            field.onChange(
                              nextLocalPart && selectedDomainName
                                ? `${nextLocalPart}@${selectedDomainName}`
                                : '',
                            );
                          }}
                          placeholder={
                            selectedDomainName
                              ? 'sender'
                              : 'Select domain first'
                          }
                          disabled={isManaged || !selectedDomainName}
                          className={errors.fromAddress ? 'border-red-500' : ''}
                        />
                        {selectedDomainName && (
                          <span className="text-sm text-muted-foreground">
                            @{selectedDomainName}
                          </span>
                        )}
                      </div>
                    );
                  }}
                />
                {errors.fromAddress && (
                  <p className="mt-1 text-sm text-red-500">
                    {errors.fromAddress.message}
                  </p>
                )}
              </div>
            </div>

            <div className="mb-4 grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <Label htmlFor="displayName">Display Name</Label>
                <Input
                  id="displayName"
                  {...register('displayName')}
                  maxLength={DISPLAY_NAME_MAX_LENGTH}
                  placeholder="IT Support Team"
                  disabled={isManaged || !selectedDomainName}
                  className={
                    errors.displayName || showDisplayNameMaxMessage
                      ? 'border-red-500'
                      : ''
                  }
                />
                {errors.displayName && (
                  <p className="mt-1 text-sm text-red-500">
                    {errors.displayName.message}
                  </p>
                )}
                {showDisplayNameMaxMessage && (
                  <p className="mt-1 text-sm text-red-500">
                    {DISPLAY_NAME_MAX_ERROR_MESSAGE}
                  </p>
                )}
              </div>
              <div className="space-y-1">
                <Label htmlFor="replyToAddress">Reply-To Address</Label>
                <Input
                  id="replyToAddress"
                  type="email"
                  {...register('replyToAddress')}
                  placeholder="reply@example.com"
                  disabled={isManaged || !selectedDomainName}
                  className={errors.replyToAddress ? 'border-red-500' : ''}
                />
                {errors.replyToAddress && (
                  <p className="mt-1 text-sm text-red-500">
                    {errors.replyToAddress.message}
                  </p>
                )}
              </div>
            </div>
          </div>

          <div className="border-t border-gray-200 pt-4">
            <h4 className="mb-4 text-sm font-medium">SMTP Configuration</h4>

            <div className="grid grid-cols-3 gap-4">
              <div className="col-span-2 space-y-1">
                <Label htmlFor="host">SMTP Host</Label>
                <Input
                  id="host"
                  {...register('host')}
                  placeholder="smtp.example.com"
                  disabled={isManaged}
                  className={errors.host ? 'border-red-500' : ''}
                />
                {errors.host && (
                  <p className="mt-1 text-sm text-red-500">
                    {errors.host.message}
                  </p>
                )}
              </div>

              <div className="space-y-1">
                <Label htmlFor="port">Port</Label>
                <Controller
                  name="port"
                  control={control}
                  render={({ field }) => (
                    <Select
                      value={String(field.value)}
                      onValueChange={value =>
                        field.onChange(parseInt(value, 10))
                      }
                      disabled={isManaged}
                    >
                      <SelectTrigger id="port">
                        <SelectValue placeholder="Select port" />
                      </SelectTrigger>
                      <SelectContent>
                        {COMMON_SMTP_PORTS.map(portOption => (
                          <SelectItem
                            key={portOption.port}
                            value={String(portOption.port)}
                          >
                            {portOption.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.port && (
                  <p className="mt-1 text-sm text-red-500">
                    {errors.port.message}
                  </p>
                )}
              </div>
            </div>
          </div>

          {shouldShowProviderType && (
            <div className="mt-4 grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <Label htmlFor="providerType">Provider Type</Label>
                <Controller
                  name="providerType"
                  control={control}
                  render={({ field }) => (
                    <Select
                      value={field.value}
                      onValueChange={field.onChange}
                      disabled={isManaged}
                    >
                      <SelectTrigger id="providerType">
                        <SelectValue placeholder="Select provider type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value={ProviderType.AWS_SES}>
                          AWS SES
                        </SelectItem>
                        <SelectItem value={ProviderType.OTHER}>
                          Other
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
            </div>
          )}

          {shouldShowCredentials && (
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <Label htmlFor="username">Username</Label>
                <Input
                  id="username"
                  {...register('username')}
                  placeholder="smtp-username"
                  disabled={isManaged}
                  className={errors.username ? 'border-destructive' : ''}
                />
                {errors.username && (
                  <p className="mt-1 text-sm text-destructive">
                    {errors.username.message}
                  </p>
                )}
              </div>

              <div className="space-y-1">
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  type="password"
                  {...register('password')}
                  placeholder={
                    isEditing ? 'Enter current password' : 'Enter password'
                  }
                  disabled={isManaged}
                  className={errors.password ? 'border-destructive' : ''}
                />
                {errors.password && (
                  <p className="mt-1 text-sm text-destructive">
                    {errors.password.message}
                  </p>
                )}
              </div>
            </div>
          )}

          <div className="space-y-1">
            <Label htmlFor="tags">Tags</Label>
            <Controller
              name="tags"
              control={control}
              render={({ field }) => {
                const selectedTags = field.value ?? [];
                const canAddMore = selectedTags.length < MAX_TAGS;

                const addTag = (rawTag: string) => {
                  const trimmedTag = rawTag.trim();
                  if (!trimmedTag || !canAddMore) {
                    return;
                  }

                  const alreadyAdded = selectedTags.some(
                    tag => tag.toLowerCase() === trimmedTag.toLowerCase(),
                  );
                  if (alreadyAdded) {
                    return;
                  }

                  field.onChange([...selectedTags, trimmedTag]);
                  setTagInput('');
                };

                const removeTag = (tagToRemove: string) => {
                  field.onChange(
                    selectedTags.filter(tag => tag !== tagToRemove),
                  );
                };

                return (
                  <div className="space-y-2">
                    <Input
                      id="tags"
                      value={tagInput}
                      onChange={event => setTagInput(event.target.value)}
                      onKeyDown={event => {
                        if (event.key === 'Enter' || event.key === ',') {
                          event.preventDefault();
                          addTag(tagInput);
                        }
                      }}
                      placeholder="Type a tag and press Enter"
                      disabled={isManaged || !canAddMore}
                    />

                    <div className="flex flex-wrap gap-2">
                      {selectedTags.map(tag => (
                        <button
                          key={tag}
                          type="button"
                          onClick={() => removeTag(tag)}
                          disabled={isManaged}
                          className="rounded-md border border-card-border bg-card px-2 py-1 text-xs hover:bg-muted disabled:cursor-not-allowed"
                        >
                          {tag} x
                        </button>
                      ))}
                    </div>

                    <p className="text-xs text-muted-foreground">
                      Tag count: {selectedTags.length}/{MAX_TAGS}
                    </p>

                    <div className="flex flex-wrap gap-2">
                      {TAG_SUGGESTIONS.map(tag => {
                        const isSelected = selectedTags.some(
                          selected =>
                            selected.toLowerCase() === tag.toLowerCase(),
                        );

                        return (
                          <button
                            key={tag}
                            type="button"
                            onClick={() => {
                              if (isSelected) {
                                removeTag(
                                  selectedTags.find(
                                    selected =>
                                      selected.toLowerCase() ===
                                      tag.toLowerCase(),
                                  ) ?? tag,
                                );
                                return;
                              }

                              addTag(tag);
                            }}
                            disabled={isManaged || (!canAddMore && !isSelected)}
                            className={`rounded-md border px-2 py-1 text-xs ${isSelected
                              ? 'border-primary bg-primary/10 text-primary'
                              : 'border-card-border bg-card hover:bg-muted'
                              } disabled:cursor-not-allowed disabled:opacity-50`}
                          >
                            {tag}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                );
              }}
            />
          </div>

          <div className="flex items-center gap-6">
            <label className="flex cursor-pointer items-center gap-2">
              <input
                type="checkbox"
                {...register('useTls')}
                disabled={isManaged}
                className="size-4 rounded border-card-border text-primary focus:ring-primary"
              />
              <span className="text-sm">Use TLS</span>
            </label>

            <label className="flex cursor-pointer items-center gap-2">
              <input
                type="checkbox"
                {...register('ignoreCertificateErrors')}
                disabled={isManaged}
                className="size-4 rounded border-card-border text-primary focus:ring-primary"
              />
              <span className="text-sm">Ignore Certificate Errors</span>
            </label>
          </div>

          {testResult && (
            <div className="rounded-lg border border-card bg-card p-4">
              <div className="flex items-center gap-2">
                {testResult.success ? (
                  <svg
                    className="size-5 text-green-500"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path
                      fillRule="evenodd"
                      d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                      clipRule="evenodd"
                    />
                  </svg>
                ) : (
                  <svg
                    className="size-5 text-destructive"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path
                      fillRule="evenodd"
                      d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                      clipRule="evenodd"
                    />
                  </svg>
                )}
                <span
                  className={`text-sm font-medium ${testResult.success ? 'text-primary' : 'text-destructive'}`}
                >
                  {testResult.message}
                </span>
                <span className="ml-auto text-xs text-muted-foreground">
                  {testResult.responseTimeMs}ms
                </span>
              </div>
            </div>
          )}

          <div className="flex justify-between gap-3 border-t border-card-border pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={handleTest}
              disabled={testing || isManaged}
            >
              {testing ? 'Testing...' : 'Test Connection'}
            </Button>

            <div className="flex gap-3">
              <Button type="button" variant="secondary" onClick={handleClose}>
                Cancel
              </Button>
              <Button
                type="submit"
                variant="default"
                disabled={saving || isManaged}
              >
                {saving
                  ? 'Saving...'
                  : isEditing
                    ? 'Update Profile'
                    : 'Create Profile'}
              </Button>
            </div>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default SenderProfileFormModal;
