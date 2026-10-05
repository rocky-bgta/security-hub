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
import {
  ISmsServerConfiguration,
  ISmsServerConfigurationForm,
  SmsServerConfigurationStatus,
  SMS_SERVER_CONFIGURATION_STATUS_LABELS,
} from 'models/SmsServerConfiguration';
import { useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import {
  API_KEY_MAX_ERROR_MESSAGE,
  API_KEY_MAX_LENGTH,
  API_SECRET_MAX_ERROR_MESSAGE,
  API_SECRET_MAX_LENGTH,
  BASE_URL_MAX_ERROR_MESSAGE,
  BASE_URL_MAX_LENGTH,
  buildSmsServerConfigurationPayload,
  buildSmsServerConfigurationUpdatePayload,
  CONFIGURATION_NAME_MAX_ERROR_MESSAGE,
  CONFIGURATION_NAME_MAX_LENGTH,
  PROVIDER_MAX_ERROR_MESSAGE,
  PROVIDER_MAX_LENGTH,
  SENDER_ID_MAX_ERROR_MESSAGE,
  SENDER_ID_MAX_LENGTH,
  SmsServerConfigurationSchema,
  SmsServerConfigurationUpdateSchema,
  smsServerConfigurationDefaultValues,
  TSmsServerConfigurationForm,
  TSmsServerConfigurationUpdateForm,
} from 'schemas/SmsServerConfigurationSchema';

interface SmsServerConfigurationFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  configuration?: ISmsServerConfiguration | null;
  getConfigurationById?: (
    id: string,
  ) => Promise<ISmsServerConfiguration | null>;
  onSave: (
    data: ISmsServerConfigurationForm,
    id?: string,
  ) => Promise<unknown | null>;
  saving?: boolean;
}

const mapConfigurationToFormValues = (
  configuration: ISmsServerConfiguration,
): TSmsServerConfigurationUpdateForm => ({
  name: configuration.name,
  provider: configuration.provider ?? '',
  apiKey: '',
  apiSecret: '',
  senderId: configuration.senderId,
  baseUrl: configuration.baseUrl,
  status: configuration.status,
  isDefault: configuration.default,
});

export const SmsServerConfigurationFormModal = ({
  isOpen,
  onClose,
  configuration,
  getConfigurationById,
  onSave,
  saving = false,
}: SmsServerConfigurationFormModalProps) => {
  const isEditing = !!configuration;
  const [loadingConfiguration, setLoadingConfiguration] = useState(false);

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TSmsServerConfigurationForm | TSmsServerConfigurationUpdateForm>({
    resolver: zodResolver(
      isEditing ? SmsServerConfigurationUpdateSchema : SmsServerConfigurationSchema,
      undefined,
      { mode: 'sync' },
    ),
    mode: 'onChange',
    reValidateMode: 'onChange',
    defaultValues: smsServerConfigurationDefaultValues,
  });

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    const loadConfiguration = async () => {
      if (!configuration) {
        reset(smsServerConfigurationDefaultValues);
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

  const handleClose = () => {
    reset(smsServerConfigurationDefaultValues);
    onClose();
  };

  const onSubmit = async (
    data: TSmsServerConfigurationForm | TSmsServerConfigurationUpdateForm,
  ) => {
    const payload = isEditing
      ? buildSmsServerConfigurationUpdatePayload(
          data as TSmsServerConfigurationUpdateForm,
        )
      : buildSmsServerConfigurationPayload(data as TSmsServerConfigurationForm);

    const result = await onSave(payload, configuration?.id);
    if (result) {
      handleClose();
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={open => !open && handleClose()}>
      <DialogContent className='max-h-[85vh] space-y-4 overflow-auto px-6'>
        <DialogTitle>
          {isEditing
            ? 'Edit SMS Server Configuration'
            : 'Create SMS Server Configuration'}
        </DialogTitle>
        <form
          onSubmit={handleSubmit(onSubmit)}
        >
          {loadingConfiguration ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              Loading configuration...
            </p>
          ) : (
            <>
              <div className="space-y-2">
                <Label htmlFor="sms-config-name">
                  Configuration Name <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="sms-config-name"
                  {...register('name')}
                  maxLength={CONFIGURATION_NAME_MAX_LENGTH}
                  placeholder="e.g. Production Twilio"
                />
                {errors.name && (
                  <p className="text-sm text-destructive">{errors.name.message}</p>
                )}
                {!errors.name && (
                  <p className="text-xs text-muted-foreground">
                    {CONFIGURATION_NAME_MAX_ERROR_MESSAGE}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="sms-config-provider">
                  Provider <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="sms-config-provider"
                  {...register('provider')}
                  maxLength={PROVIDER_MAX_LENGTH}
                  placeholder="e.g. Twilio"
                />
                {errors.provider && (
                  <p className="text-sm text-destructive">
                    {errors.provider.message}
                  </p>
                )}
                {!errors.provider && (
                  <p className="text-xs text-muted-foreground">
                    {PROVIDER_MAX_ERROR_MESSAGE}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="sms-config-api-key">
                  API Key{' '}
                  {!isEditing && <span className="text-destructive">*</span>}
                </Label>
                <Input
                  id="sms-config-api-key"
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
                <Label htmlFor="sms-config-api-secret">
                  API Secret{' '}
                  {!isEditing && <span className="text-destructive">*</span>}
                </Label>
                <Input
                  id="sms-config-api-secret"
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

              <div className="space-y-2">
                <Label htmlFor="sms-config-sender-id">
                  Sender ID <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="sms-config-sender-id"
                  {...register('senderId')}
                  maxLength={SENDER_ID_MAX_LENGTH}
                  placeholder="e.g. ASPIRE or +1234567890"
                />
                {errors.senderId && (
                  <p className="text-sm text-destructive">
                    {errors.senderId.message}
                  </p>
                )}
                {!errors.senderId && (
                  <p className="text-xs text-muted-foreground">
                    {SENDER_ID_MAX_ERROR_MESSAGE}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="sms-config-base-url">
                  Base URL <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="sms-config-base-url"
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

              <div className="space-y-2">
                <Label htmlFor="sms-config-status">
                  Status <span className="text-destructive">*</span>
                </Label>
                <Controller
                  name="status"
                  control={control}
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger id="sms-config-status">
                        <SelectValue placeholder="Select status" />
                      </SelectTrigger>
                      <SelectContent>
                        {Object.values(SmsServerConfigurationStatus).map(
                          status => (
                            <SelectItem key={status} value={status}>
                              {SMS_SERVER_CONFIGURATION_STATUS_LABELS[status]}
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

              <div className="flex items-center gap-2 mt-4">
                <Controller
                  name="isDefault"
                  control={control}
                  render={({ field }) => (
                    <Checkbox
                      id="sms-config-default"
                      checked={field.value}
                      onCheckedChange={checked =>
                        field.onChange(checked === true)
                      }
                    />
                  )}
                />
                <Label htmlFor="sms-config-default" className="cursor-pointer">
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

export default SmsServerConfigurationFormModal;
