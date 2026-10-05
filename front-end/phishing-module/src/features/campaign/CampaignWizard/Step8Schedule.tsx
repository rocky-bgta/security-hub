import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { ArrowLeftIcon, ArrowRightIcon } from 'lucide-react';
import {
  SCHEDULE_TYPE_OPTIONS,
  SENDING_PATTERN_OPTIONS,
  ScheduleType,
  SendingPattern,
} from 'models/Campaign';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useAPI } from 'hooks/UseAPI';
import {
  CampaignScheduleSchema,
  TCampaignScheduleForm,
  getCampaignScheduleFormDefaults,
} from 'schemas/CampaignSchema';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  datetimeLocalToIso,
  isoToDatetimeLocal,
  isIsoDateTimeString,
  resolveTimezoneId,
} from 'utils/Helper';
import { useStore } from 'hooks/UseStore';
import SearchSelect from 'components/SearchSelect';

interface Step8ScheduleProps {
  initialData?: Partial<TCampaignScheduleForm>;
  onSubmit: (data: TCampaignScheduleForm) => void;
  onBack: () => void;
  onPersistDraft?: (data: TCampaignScheduleForm) => void;
  isLoading?: boolean;
}

interface ITimezoneOption {
  id: string;
  timezoneId: string;
  stateName: string;
  displayName: string;
}

export const Step8Schedule = ({
  initialData,
  onSubmit,
  onBack,
  onPersistDraft,
  isLoading,
}: Step8ScheduleProps) => {
  const apiClient = useAPI();
  const { userInfo } = useStore();
  const localTimeZone = userInfo?.timeZone;

  const initialDataKey = JSON.stringify(initialData ?? null);
  const defaultValues = useMemo(
    () => getCampaignScheduleFormDefaults(initialData),
    [initialDataKey], // eslint-disable-line react-hooks/exhaustive-deps -- parent passes new object identity; initialDataKey is the stable snapshot
  );

  const lastInitialDataKeyRef = useRef(initialDataKey);
  const rawStartDateTimeRef = useRef<string | undefined>(undefined);
  const timezoneHydratedRef = useRef(false);
  const skipPersistDraftRef = useRef(false);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    reset,
    getValues,
    clearErrors,
    formState: { errors },
  } = useForm<TCampaignScheduleForm>({
    resolver: zodResolver(CampaignScheduleSchema),
    defaultValues,
  });

  useEffect(() => {
    const rawStart = initialData?.startDateTime;
    rawStartDateTimeRef.current = isIsoDateTimeString(rawStart)
      ? rawStart
      : undefined;
    timezoneHydratedRef.current = false;
    skipPersistDraftRef.current = false;
  }, [initialDataKey, initialData?.startDateTime]);

  useEffect(() => {
    if (lastInitialDataKeyRef.current !== initialDataKey) {
      lastInitialDataKeyRef.current = initialDataKey;
      reset(defaultValues);
    }
  }, [initialDataKey, defaultValues, reset]);

  useEffect(() => {
    return () => {
      if (!skipPersistDraftRef.current) {
        onPersistDraft?.(getValues());
      }
    };
  }, [getValues, onPersistDraft]);

  const scheduleType = watch('scheduleType');
  const sendingPattern = watch('sendingPattern');
  const selectedTimezone = watch('timezone');

  const [timezoneOptions, setTimezoneOptions] = useState<ITimezoneOption[]>([]);

  const isScheduled =
    scheduleType === ScheduleType.SCHEDULED ||
    initialData?.scheduleType === ScheduleType.SCHEDULED;

  const fetchTimezones = useCallback(async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_ACTIVE_TIME_ZONE_LIST,
      );
      const timezoneData = (response?.data?.data ??
        response?.data ??
        []) as ITimezoneOption[];

      setTimezoneOptions(
        timezoneData.map(item => ({
          value: item.id,
          label: item.stateName + ' - ' + item.timezoneId,
          id: item.id,
          timezoneId: item.timezoneId,
          stateName: item.stateName,
          displayName: item.displayName,
        })),
      );
    } catch (error) {
      console.error('Error fetching timezone options:', error);
    }
  }, [apiClient]);

  useEffect(() => {
    if (isScheduled) {
      void fetchTimezones();
    }
  }, [isScheduled, fetchTimezones]);

  useEffect(() => {
    if (timezoneOptions.length === 0 || timezoneHydratedRef.current) return;

    const storedTimezone = getValues('timezone');
    const resolvedTimezoneId = resolveTimezoneId(
      storedTimezone,
      timezoneOptions,
    );
    const ianaTimeZone = timezoneOptions.find(
      option => option.id === resolvedTimezoneId,
    )?.timezoneId;

    if (resolvedTimezoneId && resolvedTimezoneId !== storedTimezone) {
      setValue('timezone', resolvedTimezoneId, { shouldDirty: false });
    }

    const rawStartDateTime = rawStartDateTimeRef.current;
    if (rawStartDateTime && ianaTimeZone) {
      setValue(
        'startDateTime',
        isoToDatetimeLocal(rawStartDateTime, ianaTimeZone),
        {
          shouldDirty: false,
        },
      );
    }

    timezoneHydratedRef.current = true;
  }, [timezoneOptions, getValues, setValue]);

  const handleFormSubmit = (data: TCampaignScheduleForm) => {
    skipPersistDraftRef.current = true;

    const ianaTimeZone = timezoneOptions.find(
      option => option.id === data.timezone,
    )?.timezoneId;

    const schedulePayload: TCampaignScheduleForm = {
      ...data,
      startDateTime: datetimeLocalToIso(data.startDateTime, ianaTimeZone),
      endDateTime: data.endDateTime
        ? datetimeLocalToIso(data.endDateTime, ianaTimeZone)
        : '',
    };

    if (data.sendingPattern === SendingPattern.ALL_AT_ONCE) {
      schedulePayload.batchSize = undefined;
      schedulePayload.batchIntervalMinutes = undefined;
    }

    onSubmit(schedulePayload);
  };

  return (
    <form
      onSubmit={handleSubmit(handleFormSubmit)}
      className="mx-auto max-w-3xl"
    >
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-foreground">
          Schedule Campaign
        </h2>
        <p className="mt-2 text-muted-foreground">
          Configure when and how to send the phishing emails.
        </p>
      </div>

      {/* Schedule Type */}
      <div className="mb-6">
        <label className="mb-3 block text-sm font-medium text-foreground">
          When to Send
        </label>
        <div className="space-y-3">
          {SCHEDULE_TYPE_OPTIONS.map(option => (
            <label
              key={option.value}
              className={`flex cursor-pointer items-start rounded-lg border p-4 transition-colors ${
                scheduleType === option.value
                  ? 'border-primary bg-primary/10 ring-2 ring-primary/20'
                  : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <input
                type="radio"
                value={option.value}
                checked={scheduleType === option.value}
                onChange={() => {
                  const next = option.value;
                  setValue('scheduleType', next);
                  setValue('timezone', localTimeZone ?? '', {
                    shouldDirty: true,
                    shouldValidate: true,
                    shouldTouch: true,
                  });
                  if (next !== ScheduleType.SCHEDULED) {
                    setValue('startDateTime', '');
                    setValue('endDateTime', '');
                    clearErrors(['startDateTime']);
                  }
                }}
                className="mt-0.5 size-4 border-primary text-primary"
              />
              <div className="ml-3">
                <span className="block font-medium text-foreground">
                  {option.label}
                </span>
                <span className="block text-sm text-muted-foreground">
                  {option.description}
                </span>
              </div>
            </label>
          ))}
        </div>
      </div>

      {/* Date/Time Selection (for SCHEDULED) */}
      {scheduleType === ScheduleType.SCHEDULED && (
        <div className="mb-6 space-y-4">
          <div className="grid grid-cols-3 gap-4">
            <div className="col-span-1">
              <label className="mb-2 block text-sm font-medium text-foreground">
                Start Date & Time <span className="text-destructive">*</span>
              </label>
              <Input
                {...register('startDateTime')}
                type="datetime-local"
                className="w-full"
                onClick={e => {
                  const input = e.target as HTMLInputElement;
                  input.showPicker?.();
                }}
              />
              {errors.startDateTime && (
                <p className="mt-1 text-sm text-destructive">
                  {errors.startDateTime.message}
                </p>
              )}
            </div>
            <div className="col-span-2">
              <label className="mb-2 block text-sm font-medium text-foreground">
                Timezone
              </label>
              <SearchSelect
                items={timezoneOptions.map(item => ({
                  value: item.id,
                  label:
                    item.displayName +
                    ' (' +
                    item.stateName +
                    ' - ' +
                    item.timezoneId +
                    ')',
                }))}
                value={selectedTimezone}
                onValueChange={value =>
                  setValue('timezone', value, { shouldDirty: true })
                }
                placeholder="Select Timezone"
              />
            </div>
          </div>
        </div>
      )}

      {/* Recurring Configuration (for RECURRING) */}
      {scheduleType === ScheduleType.RECURRING && (
        <div className="mb-6 rounded-lg border border-card-border bg-card-background p-4">
          <p className="text-sm text-muted-foreground">
            Recurring campaign configuration coming soon. Please select a
            different schedule type.
          </p>
        </div>
      )}

      {/* Sending Pattern */}
      <div className="mb-6">
        <label className="mb-3 block text-sm font-medium text-foreground">
          Sending Pattern
        </label>
        <div className="grid grid-cols-2 gap-4">
          {SENDING_PATTERN_OPTIONS.map(option => (
            <button
              key={option.value}
              type="button"
              onClick={() => {
                setValue('sendingPattern', option.value, {
                  shouldDirty: true,
                  shouldValidate: true,
                });
                if (option.value === SendingPattern.ALL_AT_ONCE) {
                  setValue('batchSize', undefined, { shouldValidate: true });
                  setValue('batchIntervalMinutes', undefined, {
                    shouldValidate: true,
                  });
                  clearErrors(['batchSize', 'batchIntervalMinutes']);
                }
              }}
              className={`rounded-lg border p-4 text-left transition-colors ${
                sendingPattern === option.value
                  ? 'border-primary bg-primary/10 ring-2 ring-primary/20'
                  : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <span className="block font-medium text-foreground">
                {option.label}
              </span>
              <span className="mt-1 block text-sm text-muted-foreground">
                {option.description}
              </span>
            </button>
          ))}
        </div>
      </div>

      {/* Staggered Configuration */}
      {sendingPattern === SendingPattern.STAGGERED && (
        <div className="mb-6 rounded-lg p-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-2 block text-sm font-medium text-foreground">
                Batch Size <span className="text-destructive">*</span>
              </label>
              <Input
                type="number"
                {...register('batchSize', { valueAsNumber: true })}
                placeholder="e.g., 50"
                min={1}
                className="w-full"
              />
              {errors.batchSize && (
                <p className="mt-1 text-sm text-destructive">
                  {errors.batchSize.message}
                </p>
              )}
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium text-foreground">
                Interval (minutes)
              </label>
              <Input
                type="number"
                {...register('batchIntervalMinutes', { valueAsNumber: true })}
                placeholder="e.g., 30"
                min={1}
                className="w-full"
              />
            </div>
          </div>
          <p className="mt-2 text-xs text-muted-foreground">
            Emails will be sent in batches to avoid overwhelming mail servers.
          </p>
        </div>
      )}

      {/* Actions */}
      <div className="flex justify-between border-t border-card-border pt-6">
        <Button type="button" variant="outline" onClick={onBack}>
          <ArrowLeftIcon className="size-4" />
          Back
        </Button>
        <Button type="submit" variant="default" disabled={isLoading}>
          {isLoading ? 'Saving...' : 'Save & Continue'}
          <ArrowRightIcon className="size-4" />
        </Button>
      </div>
    </form>
  );
};
