import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import { Dialog, DialogContent, DialogTitle } from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import SearchSelect from 'components/SearchSelect';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import {
  IVoiceServerConfiguration,
  IVoiceServerConfigurationForm,
  VoiceProvider,
  VoiceServerConfigurationStatus,
  VOICE_PROVIDER_LABELS,
  VOICE_SERVER_CONFIGURATION_STATUS_LABELS,
} from 'models/VoiceServerConfiguration';
import { useEffect, useMemo, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  API_KEY_MAX_ERROR_MESSAGE,
  API_KEY_MAX_LENGTH,
  API_SECRET_MAX_ERROR_MESSAGE,
  API_SECRET_MAX_LENGTH,
  BASE_URL_MAX_ERROR_MESSAGE,
  BASE_URL_MAX_LENGTH,
  buildVoiceServerConfigurationPayload,
  buildVoiceServerConfigurationUpdatePayload,
  CALLER_ID_MAX_ERROR_MESSAGE,
  CALLER_ID_MAX_LENGTH,
  CONFIGURATION_NAME_MAX_ERROR_MESSAGE,
  CONFIGURATION_NAME_MAX_LENGTH,
  TVoiceServerConfigurationForm,
  TVoiceServerConfigurationUpdateForm,
  VoiceServerConfigurationSchema,
  VoiceServerConfigurationUpdateSchema,
  voiceServerConfigurationDefaultValues,
} from 'schemas/VoiceServerConfigurationSchema';

interface VoiceServerConfigurationFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  configuration?: IVoiceServerConfiguration | null;
  getConfigurationById?: (
    id: string,
  ) => Promise<IVoiceServerConfiguration | null>;
  onSave: (
    data: IVoiceServerConfigurationForm,
    id?: string,
  ) => Promise<unknown | null>;
  saving?: boolean;
}

interface ICountryOption {
  id: string;
  name: string;
  code: string;
}

const toVoiceProvider = (provider?: string): VoiceProvider => {
  const normalized = (provider || '').toUpperCase();
  return Object.values(VoiceProvider).includes(normalized as VoiceProvider)
    ? (normalized as VoiceProvider)
    : VoiceProvider.TWILIO;
};

const mapConfigurationToFormValues = (
  configuration: IVoiceServerConfiguration,
): TVoiceServerConfigurationUpdateForm => ({
  name: configuration.name,
  provider: toVoiceProvider(configuration.provider),
  apiKey: '',
  apiSecret: '',
  callerId: configuration.callerId || '',
  baseUrl: configuration.baseUrl || '',
  region: configuration.region || '',
  countryCode: configuration.countryCode || 'US',
  status: configuration.status || VoiceServerConfigurationStatus.ACTIVE,
  isDefault: configuration.default,
});

export const VoiceServerConfigurationFormModal = ({
  isOpen,
  onClose,
  configuration,
  getConfigurationById,
  onSave,
  saving = false,
}: VoiceServerConfigurationFormModalProps) => {
  const isEditing = !!configuration;
  const { get } = useAPI();
  const [loadingConfiguration, setLoadingConfiguration] = useState(false);
  const [countries, setCountries] = useState<ICountryOption[]>([]);
  const [loadingCountries, setLoadingCountries] = useState(false);

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<
    TVoiceServerConfigurationForm | TVoiceServerConfigurationUpdateForm
  >({
    resolver: zodResolver(
      isEditing
        ? VoiceServerConfigurationUpdateSchema
        : VoiceServerConfigurationSchema,
      undefined,
      { mode: 'sync' },
    ),
    mode: 'onChange',
    reValidateMode: 'onChange',
    defaultValues: voiceServerConfigurationDefaultValues,
  });

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    const loadConfiguration = async () => {
      if (!configuration) {
        reset(voiceServerConfigurationDefaultValues);
        return;
      }

      setLoadingConfiguration(true);
      try {
        const freshConfiguration = getConfigurationById
          ? await getConfigurationById(configuration.id)
          : configuration;

        if (freshConfiguration) {
          reset(mapConfigurationToFormValues(freshConfiguration));
        } else {
          reset(mapConfigurationToFormValues(configuration));
        }
      } finally {
        setLoadingConfiguration(false);
      }
    };

    void loadConfiguration();
  }, [isOpen, configuration, getConfigurationById, reset]);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    let active = true;

    const loadCountries = async () => {
      setLoadingCountries(true);
      try {
        const response = (await get(API_END_POINTS.GET_ACTIVE_COUNTRY_LIST)) as
          | IResponse<ICountryOption[]>
          | undefined;

        if (!active) {
          return;
        }

        const items = Array.isArray(response?.data) ? response.data : [];
        setCountries(
          items
            .filter(item => item.code && item.name)
            .sort((a, b) => a.name.localeCompare(b.name)),
        );
      } catch (error) {
        console.error('Error fetching country list:', error);
        if (active) {
          setCountries([]);
        }
      } finally {
        if (active) {
          setLoadingCountries(false);
        }
      }
    };

    void loadCountries();

    return () => {
      active = false;
    };
  }, [get, isOpen]);

  const countryItems = useMemo(
    () =>
      countries.map(country => ({
        value: country.code.toUpperCase(),
        label: country.name,
      })),
    [countries],
  );

  const handleClose = () => {
    reset(voiceServerConfigurationDefaultValues);
    onClose();
  };

  const onSubmit = async (
    data: TVoiceServerConfigurationForm | TVoiceServerConfigurationUpdateForm,
  ) => {
    const payload = isEditing
      ? buildVoiceServerConfigurationUpdatePayload(
          data as TVoiceServerConfigurationUpdateForm,
        )
      : buildVoiceServerConfigurationPayload(
          data as TVoiceServerConfigurationForm,
        );

    const result = await onSave(payload, configuration?.id);
    if (result) {
      handleClose();
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={open => !open && handleClose()}>
      <DialogContent className="max-h-[85vh] w-full max-w-xl space-y-4 overflow-y-auto px-6">
        <DialogTitle>
          {isEditing
            ? 'Edit Voice Server Configuration'
            : 'Create Voice Server Configuration'}
        </DialogTitle>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {loadingConfiguration ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              Loading configuration...
            </p>
          ) : (
            <>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="voice-config-name">
                    Configuration Name{' '}
                    <span className="text-destructive">*</span>
                  </Label>
                  <Input
                    id="voice-config-name"
                    {...register('name')}
                    maxLength={CONFIGURATION_NAME_MAX_LENGTH}
                    placeholder="e.g. Production Twilio Voice"
                  />
                  {errors.name && (
                    <p className="text-sm text-destructive">
                      {errors.name.message}
                    </p>
                  )}
                  {!errors.name && (
                    <p className="text-xs text-muted-foreground">
                      {CONFIGURATION_NAME_MAX_ERROR_MESSAGE}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="voice-config-provider">
                    Provider <span className="text-destructive">*</span>
                  </Label>
                  <Controller
                    name="provider"
                    control={control}
                    render={({ field }) => (
                      <Select
                        value={field.value}
                        onValueChange={field.onChange}
                      >
                        <SelectTrigger id="voice-config-provider">
                          <SelectValue placeholder="Select provider" />
                        </SelectTrigger>
                        <SelectContent>
                          {Object.values(VoiceProvider).map(provider => (
                            <SelectItem key={provider} value={provider}>
                              {VOICE_PROVIDER_LABELS[provider]}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    )}
                  />
                  {errors.provider && (
                    <p className="text-sm text-destructive">
                      {errors.provider.message}
                    </p>
                  )}
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="voice-config-api-key">
                  API Key{' '}
                  {!isEditing && <span className="text-destructive">*</span>}
                </Label>
                <Input
                  id="voice-config-api-key"
                  type="password"
                  autoComplete="off"
                  {...register('apiKey')}
                  maxLength={API_KEY_MAX_LENGTH}
                  placeholder={
                    isEditing
                      ? 'Leave blank to keep existing API key'
                      : 'Enter API key'
                  }
                />
                {errors.apiKey && (
                  <p className="text-sm text-destructive">
                    {errors.apiKey.message}
                  </p>
                )}
                {!errors.apiKey && (
                  <p className="text-xs text-muted-foreground">
                    {isEditing
                      ? 'Leave blank to keep existing credentials'
                      : API_KEY_MAX_ERROR_MESSAGE}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="voice-config-api-secret">
                  API Secret{' '}
                  {!isEditing && <span className="text-destructive">*</span>}
                </Label>
                <Input
                  id="voice-config-api-secret"
                  type="password"
                  autoComplete="off"
                  {...register('apiSecret')}
                  maxLength={API_SECRET_MAX_LENGTH}
                  placeholder={
                    isEditing
                      ? 'Leave blank to keep existing API secret'
                      : 'Enter API secret'
                  }
                />
                {errors.apiSecret && (
                  <p className="text-sm text-destructive">
                    {errors.apiSecret.message}
                  </p>
                )}
                {!errors.apiSecret && (
                  <p className="text-xs text-muted-foreground">
                    {isEditing
                      ? 'Leave blank to keep existing credentials'
                      : API_SECRET_MAX_ERROR_MESSAGE}
                  </p>
                )}
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="voice-config-caller-id">
                    Caller ID <span className="text-destructive">*</span>
                  </Label>
                  <Input
                    id="voice-config-caller-id"
                    {...register('callerId')}
                    maxLength={CALLER_ID_MAX_LENGTH}
                    placeholder="e.g. +19176009233"
                  />
                  {errors.callerId && (
                    <p className="text-sm text-destructive">
                      {errors.callerId.message}
                    </p>
                  )}
                  {!errors.callerId && (
                    <p className="text-xs text-muted-foreground">
                      {CALLER_ID_MAX_ERROR_MESSAGE}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="voice-config-base-url">Base URL</Label>
                  <Input
                    id="voice-config-base-url"
                    {...register('baseUrl')}
                    maxLength={BASE_URL_MAX_LENGTH}
                    placeholder="https://api.twilio.com"
                  />
                  {errors.baseUrl && (
                    <p className="text-sm text-destructive">
                      {errors.baseUrl.message}
                    </p>
                  )}
                  {!errors.baseUrl && (
                    <p className="text-xs text-muted-foreground">
                      {BASE_URL_MAX_ERROR_MESSAGE}
                    </p>
                  )}
                </div>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="voice-config-region">Region</Label>
                  <Input
                    id="voice-config-region"
                    {...register('region')}
                    placeholder="e.g. us1"
                  />
                  {errors.region && (
                    <p className="text-sm text-destructive">
                      {errors.region.message}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="voice-config-country">Country</Label>
                  <Controller
                    name="countryCode"
                    control={control}
                    render={({ field }) => {
                      const selectedCode = (field.value || '').toUpperCase();
                      const items =
                        selectedCode &&
                        !countryItems.some(item => item.value === selectedCode)
                          ? [
                              ...countryItems,
                              { value: selectedCode, label: selectedCode },
                            ]
                          : countryItems;

                      return (
                        <SearchSelect
                          items={items}
                          value={selectedCode || undefined}
                          onValueChange={field.onChange}
                          placeholder={
                            loadingCountries
                              ? 'Loading countries...'
                              : 'Search country'
                          }
                          disabled={loadingCountries}
                          hasError={!!errors.countryCode}
                        />
                      );
                    }}
                  />
                  {errors.countryCode && (
                    <p className="text-sm text-destructive">
                      {errors.countryCode.message}
                    </p>
                  )}
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="voice-config-status">
                  Status <span className="text-destructive">*</span>
                </Label>
                <Controller
                  name="status"
                  control={control}
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger id="voice-config-status">
                        <SelectValue placeholder="Select status" />
                      </SelectTrigger>
                      <SelectContent>
                        {Object.values(VoiceServerConfigurationStatus).map(
                          status => (
                            <SelectItem key={status} value={status}>
                              {VOICE_SERVER_CONFIGURATION_STATUS_LABELS[status]}
                            </SelectItem>
                          ),
                        )}
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.status && (
                  <p className="text-sm text-destructive">
                    {errors.status.message}
                  </p>
                )}
              </div>

              <div className="mt-4 flex items-center gap-2">
                <Controller
                  name="isDefault"
                  control={control}
                  render={({ field }) => (
                    <Checkbox
                      id="voice-config-default"
                      checked={field.value}
                      onCheckedChange={checked =>
                        field.onChange(checked === true)
                      }
                    />
                  )}
                />
                <Label
                  htmlFor="voice-config-default"
                  className="cursor-pointer"
                >
                  Set as default configuration
                </Label>
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <Button type="button" variant="secondary" onClick={handleClose}>
                  Cancel
                </Button>
                <Button type="submit" disabled={saving || loadingConfiguration}>
                  {saving
                    ? isEditing
                      ? 'Saving...'
                      : 'Creating...'
                    : isEditing
                      ? 'Save Changes'
                      : 'Create Configuration'}
                </Button>
              </div>
            </>
          )}
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default VoiceServerConfigurationFormModal;
