import { useCallback, useEffect, useRef, useState } from 'react';
import {
  AlertCircle,
  BookOpen,
  Calendar,
  FileText,
  Mail,
  Mic,
  Pencil,
  PhoneForwarded,
  Plus,
  Send,
  Sparkles,
  Users,
  X,
} from 'lucide-react';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import SearchSelect from 'components/SearchSelect';
import type {
  CampaignSetup,
  CampaignType,
  IVishingCampaign,
  IVishingScenario,
  IVoiceServerConfiguration,
  LearningMode,
  ResponseStage,
  StepKey,
} from 'models/Vishing';
import {
  cn,
  isoToDatetimeLocal,
  resolveTimezoneId,
  type ITimezoneLookupOption,
} from 'utils/Helper';
import { Settings2 } from 'lucide-react';
import { PackageSelectField } from 'features/campaign/CampaignWizard/PackageSelectField';
import {
  toVishingScheduleRequest,
  scheduleToUiState,
  type UiScheduleWhen,
  type UiSendingPattern,
} from 'schemas/VishingSchema';
import { useVishingWizard } from '../context/VishingWizardContext';
import { useVishingCampaigns } from 'hooks/UseVishingCampaigns';
import {
  CampaignChannel,
  CampaignValidityUnit,
  formatCampaignExpireSummary,
} from 'models/Campaign';
import { useVishingScenarios } from 'hooks/UseVishingScenarios';
import { useVoiceServerConfigurations } from 'hooks/UseVoiceServerConfigurations';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface ITimezoneOption extends ITimezoneLookupOption {
  stateName: string;
}

export function SetupTab({
  setup,
  onChange,
}: {
  setup: CampaignSetup;
  onChange: (s: CampaignSetup) => void;
}) {
  const MAX = 50;
  const types: { id: CampaignType; title: string; desc: string }[] = [
    {
      id: 'VISHING_SIMULATION',
      title: 'AI Vishing Simulation',
      desc: 'Send AI Vishing Simulation Without Training',
    },
    {
      id: 'VISHING_WITH_TRAINING',
      title: 'AI Vishing with Training',
      desc: 'Send AI Vishing Simulation and assign training to users who fail.',
    },
  ];

  const validityPeriodMax =
    setup.expireDate.validityUnit === CampaignValidityUnit.MONTHS ? 12 : 365;
  const validityPeriod = setup.expireDate.validityPeriod;
  const isValidityPeriodTooLow =
    !Number.isFinite(validityPeriod) || validityPeriod <= 0;
  const isValidityPeriodTooHigh = validityPeriod > validityPeriodMax;
  const isValidityPeriodInvalid =
    isValidityPeriodTooLow || isValidityPeriodTooHigh;

  return (
    <div className="mx-auto max-w-2xl p-6 sm:p-8">
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-white">Campaign Setup</h2>
        <p className="mt-1 text-sm text-white/50">
          Enter a name and select the campaign type.
        </p>
      </div>

      {/* Name */}
      <div className="mb-6 space-y-2">
        <div className="flex items-center justify-between">
          <Label htmlFor="campaign-name" className="text-sm text-white/80">
            Campaign Name <span className="text-vibrant-red">*</span>
          </Label>
        </div>
        <Input
          id="campaign-name"
          value={setup.name}
          maxLength={MAX}
          onChange={e => onChange({ ...setup, name: e.target.value })}
          placeholder="Enter campaign name"
        />

        <div className="mt-1 flex justify-end">
          <span
            className={`text-sm ${setup.name.length > MAX ? 'font-medium text-vibrant-red' : 'text-muted-foreground'}`}
          >
            {setup.name.length}/{MAX}
          </span>
        </div>
      </div>

      {/* Type */}
      <div className="space-y-3">
        <Label className="text-sm text-white/80">Campaign Type</Label>
        <div className="grid gap-3 sm:grid-cols-2">
          {types.map(t => {
            const selected = setup.type === t.id;
            return (
              <button
                key={t.id}
                type="button"
                onClick={() => onChange({ ...setup, type: t.id })}
                className={cn(
                  'group relative rounded-xl border p-4 text-left transition-all duration-200',
                  selected
                    ? 'border-[#00FFA3]/70 bg-[#00FFA3]/[0.06] shadow-[0_0_24px_rgba(0,255,163,0.25)]'
                    : 'border-white/10 bg-white/[0.02] hover:border-white/20 hover:bg-white/[0.04]',
                )}
              >
                <div className="flex items-start gap-3">
                  <span
                    className={cn(
                      'mt-0.5 grid h-4 w-4 shrink-0 place-items-center rounded-full border transition-colors',
                      selected ? 'border-[#00FFA3]' : 'border-white/30',
                    )}
                  >
                    {selected && (
                      <span className="size-2 rounded-full bg-[#00FFA3] shadow-[0_0_8px_rgba(0,255,163,0.8)]" />
                    )}
                  </span>
                  <div className="min-w-0">
                    <div className="text-sm font-medium text-white">
                      {t.title}
                    </div>
                    <div className="mt-1 text-xs leading-relaxed text-white/50">
                      {t.desc}
                    </div>
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      <div className="mt-8 border-t border-white/10 pt-6">
        <PackageSelectField
          channel={CampaignChannel.VOICE}
          productPackageId={setup.productPackageId}
          onChange={({ productPackageId }) =>
            onChange({ ...setup, productPackageId })
          }
        />
      </div>

      {/* Response stages for all types; learning mode only for training */}
      <ResponseStageSection
        setup={setup}
        onChange={onChange}
        showLearningMode={setup.type === 'VISHING_WITH_TRAINING'}
      />

      <div className="my-8 grid grid-cols-2 items-start gap-5 border-t border-white/10 pt-6">
        <div className="space-y-2">
          <Label className="text-sm text-white/80">
            Simulation activity period{' '}
            <span className="text-vibrant-red">*</span>
          </Label>
          <Select
            value={setup.expireDate.validityUnit}
            onValueChange={value =>
              onChange({
                ...setup,
                expireDate: {
                  ...setup.expireDate,
                  validityUnit: value as CampaignValidityUnit,
                },
              })
            }
          >
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Select validity unit" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={CampaignValidityUnit.DAYS}>Days</SelectItem>
              <SelectItem value={CampaignValidityUnit.MONTHS}>
                Months
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="validityPeriod" className="text-sm text-white/80">
            Period length <span className="text-vibrant-red">*</span>
          </Label>
          <Input
            id="validityPeriod"
            type="number"
            min={1}
            max={validityPeriodMax}
            step={1}
            value={Number.isFinite(validityPeriod) ? validityPeriod : ''}
            onChange={e =>
              onChange({
                ...setup,
                expireDate: {
                  ...setup.expireDate,
                  validityPeriod:
                    e.target.value === '' ? NaN : Number(e.target.value),
                },
              })
            }
            placeholder="Enter period length"
            className={`w-full ${
              isValidityPeriodInvalid
                ? 'border-vibrant-red bg-vibrant-red/10'
                : 'border-card-border'
            }`}
          />
          <p className="mt-1 text-sm text-muted-foreground">
            Between 1 and {validityPeriodMax}{' '}
            {setup.expireDate.validityUnit === CampaignValidityUnit.MONTHS
              ? 'months'
              : 'days'}
            .
          </p>
          {isValidityPeriodTooLow ? (
            <p className="mt-1 text-sm text-vibrant-red">
              This value must be greater than 0
            </p>
          ) : null}
          {isValidityPeriodTooHigh ? (
            <p className="mt-1 text-sm text-vibrant-red">
              Maximum {validityPeriodMax}{' '}
              {setup.expireDate.validityUnit === CampaignValidityUnit.MONTHS
                ? 'months'
                : 'days'}
            </p>
          ) : null}
        </div>
      </div>
    </div>
  );
}

const STAGES: {
  id: ResponseStage;
  title: string;
  desc: string;
  trainingDesc: string;
  badge?: string;
}[] = [
  {
    id: 'CALL_ENGAGED',
    title: 'Call Engaged',
    desc: 'Tracks users who answer and engage with the simulated vishing call for 30 seconds or longer.',
    trainingDesc:
      'Tracks users who answer and engage with the simulated vishing call for 30 seconds or longer. Micro training is automatically assigned upon detection.',
  },
  {
    id: 'COMPROMISED',
    title: 'Compromised',
    desc: 'Tracks users who disclose credentials or sensitive information during the simulated vishing call.',
    trainingDesc:
      'Tracks users who disclose credentials or sensitive information during the simulated vishing call. Micro training is automatically assigned upon detection.',
    badge: 'Recommended',
  },
];

const LEARNING_MODES: { id: LearningMode; title: string; desc: string }[] = [
  {
    id: 'MICRO_CONTENT',
    title: 'Micro Content',
    desc: 'Short training module, duration not exceeding 3 minutes.',
  },
];

function ResponseStageSection({
  setup,
  onChange,
  showLearningMode,
}: {
  setup: CampaignSetup;
  onChange: (s: CampaignSetup) => void;
  showLearningMode: boolean;
}) {
  const toggleStage = (id: ResponseStage) => {
    const has = setup.stages.includes(id);
    onChange({
      ...setup,
      stages: has ? setup.stages.filter(s => s !== id) : [...setup.stages, id],
    });
  };
  const error = setup.stages.length === 0;

  return (
    <div className="mt-8 border-t border-white/10 pt-6">
      <div className="mb-5">
        <h3 className="text-base font-semibold text-white">
          {showLearningMode
            ? 'Phishing Response Stage & Learning Mode'
            : 'Phishing Response Stage'}
        </h3>
        <p className="mt-1 text-xs text-white/50">
          {showLearningMode
            ? 'Select at least one response stage and a learning mode.'
            : 'Select at least one response stage.'}
        </p>
      </div>

      <div className="space-y-3">
        <Label className="text-sm text-white/80">
          Response Stages <span className="text-[#00FFA3]">*</span>
        </Label>
        <div className="grid gap-3 sm:grid-cols-2">
          {STAGES.map(s => {
            const selected = setup.stages.includes(s.id);
            return (
              <button
                key={s.id}
                type="button"
                onClick={() => toggleStage(s.id)}
                title={showLearningMode ? s.trainingDesc : s.desc}
                className={cn(
                  'group relative rounded-xl border p-4 text-left transition-all duration-200 hover:-translate-y-0.5',
                  selected
                    ? 'border-[#00FFA3]/70 bg-[#00FFA3]/[0.06] shadow-[0_0_24px_rgba(0,255,163,0.25)]'
                    : 'border-white/10 bg-white/[0.02] hover:border-white/20 hover:bg-white/[0.04]',
                )}
              >
                <div className="flex items-start gap-3">
                  <span
                    className={cn(
                      'mt-0.5 grid h-4 w-4 shrink-0 place-items-center rounded-[4px] border transition-colors',
                      selected
                        ? 'border-[#00FFA3] bg-[#00FFA3]/20'
                        : 'border-white/30',
                    )}
                  >
                    {selected && (
                      <svg
                        viewBox="0 0 12 12"
                        className="size-3 text-[#00FFA3]"
                      >
                        <path
                          fill="currentColor"
                          d="M10.28 3.22a.75.75 0 0 1 0 1.06l-5 5a.75.75 0 0 1-1.06 0l-2.5-2.5a.75.75 0 1 1 1.06-1.06L4.75 7.69l4.47-4.47a.75.75 0 0 1 1.06 0Z"
                        />
                      </svg>
                    )}
                  </span>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between gap-2">
                      <div className="text-sm font-medium text-white">
                        {s.title}
                      </div>
                      {s.badge && (
                        <span className="rounded-full border border-[#00FFA3]/40 bg-[#00FFA3]/10 px-2 py-0.5 text-[10px] font-medium text-[#00FFA3]">
                          {s.badge}
                        </span>
                      )}
                    </div>
                    <div className="mt-1 text-xs leading-relaxed text-white/50">
                      {showLearningMode ? s.trainingDesc : s.desc}
                    </div>
                  </div>
                </div>
              </button>
            );
          })}
        </div>
        {error && (
          <p className="text-xs text-red-400">
            Please select at least one response stage.
          </p>
        )}
      </div>

      {showLearningMode ? (
        <div className="mt-6 space-y-3">
          <Label className="text-sm text-white/80">Learning Mode</Label>
          <div className="grid gap-3 sm:grid-cols-2">
            {LEARNING_MODES.map(m => {
              const selected = setup.learningMode === m.id;
              return (
                <button
                  key={m.id}
                  type="button"
                  onClick={() => onChange({ ...setup, learningMode: m.id })}
                  title={m.desc}
                  className={cn(
                    'group relative rounded-xl border p-4 text-left transition-all duration-200 hover:-translate-y-0.5',
                    selected
                      ? 'border-[#00FFA3]/70 bg-[#00FFA3]/[0.06] shadow-[0_0_24px_rgba(0,255,163,0.25)]'
                      : 'border-white/10 bg-white/[0.02] hover:border-white/20 hover:bg-white/[0.04]',
                  )}
                >
                  <div className="flex items-start gap-3">
                    <span
                      className={cn(
                        'mt-0.5 grid h-4 w-4 shrink-0 place-items-center rounded-full border transition-colors',
                        selected ? 'border-[#00FFA3]' : 'border-white/30',
                      )}
                    >
                      {selected && (
                        <span className="size-2 rounded-full bg-[#00FFA3] shadow-[0_0_8px_rgba(0,255,163,0.8)]" />
                      )}
                    </span>
                    <div className="min-w-0">
                      <div className="text-sm font-medium text-white">
                        {m.title}
                      </div>
                      <div className="mt-1 text-xs leading-relaxed text-white/50">
                        {m.desc}
                      </div>
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      ) : null}
    </div>
  );
}

type WhenOption = 'immediate' | 'later' | 'recurring';
type PatternOption = 'all' | 'staggered';

export function SchedulePlaceholder() {
  const { setSchedule, schedule: contextSchedule } = useVishingWizard();
  const apiClient = useAPI();
  const { userInfo } = useStore();
  const initialSchedule = scheduleToUiState(contextSchedule);
  const [when, setWhen] = useState<UiScheduleWhen>(initialSchedule.when);
  const [startDateTime, setStartDateTime] = useState(
    initialSchedule.startDateTime,
  );
  const [timezone, setTimezone] = useState(initialSchedule.timezone);
  const [pattern, setPattern] = useState<UiSendingPattern>(
    initialSchedule.pattern,
  );
  const [timezoneOptions, setTimezoneOptions] = useState<ITimezoneOption[]>([]);
  const timezoneHydratedRef = useRef(false);
  const rawScheduledAtRef = useRef(contextSchedule.startDateTime);

  const fetchTimezones = useCallback(async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_ACTIVE_TIME_ZONE_LIST,
      );
      const timezoneData = (response?.data?.data ??
        response?.data ??
        []) as ITimezoneOption[];
      setTimezoneOptions(timezoneData);
    } catch (error) {
      console.error('Error fetching timezone options:', error);
    }
  }, [apiClient]);

  useEffect(() => {
    if (when === 'later') {
      // eslint-disable-next-line react-hooks/set-state-in-effect -- load timezone dropdown options
      void fetchTimezones();
    }
  }, [when, fetchTimezones]);

  useEffect(() => {
    if (timezoneOptions.length === 0 || timezoneHydratedRef.current) return;

    const resolvedTimezoneId = resolveTimezoneId(
      timezone || userInfo?.timeZone || '',
      timezoneOptions,
    );
    const ianaTimeZone = timezoneOptions.find(
      option => option.id === resolvedTimezoneId,
    )?.timezoneId;

    if (resolvedTimezoneId && resolvedTimezoneId !== timezone) {
      // eslint-disable-next-line react-hooks/set-state-in-effect -- hydrate stored timezone id
      setTimezone(resolvedTimezoneId);
    }

    const rawScheduledAt = rawScheduledAtRef.current;
    if (rawScheduledAt && ianaTimeZone) {
      setStartDateTime(isoToDatetimeLocal(rawScheduledAt, ianaTimeZone));
    }

    timezoneHydratedRef.current = true;
  }, [timezoneOptions, timezone, userInfo?.timeZone]);

  useEffect(() => {
    const ianaTimeZone = timezoneOptions.find(
      option => option.id === timezone,
    )?.timezoneId;

    setSchedule(
      toVishingScheduleRequest({
        when,
        startDateTime,
        timezone,
        pattern,
        ianaTimeZone,
      }),
    );
  }, [when, startDateTime, timezone, pattern, timezoneOptions, setSchedule]);

  const whenOptions: {
    id: WhenOption;
    title: string;
    desc: string;
    icon: typeof Send;
  }[] = [
    {
      id: 'immediate',
      title: 'Call Immediately',
      desc: 'Start calling right away',
      icon: Send,
    },
    {
      id: 'later',
      title: 'Schedule for Later',
      desc: 'Choose a specific date and time',
      icon: Calendar,
    },
  ];

  const patternOptions: { id: PatternOption; title: string; desc: string }[] = [
    { id: 'all', title: 'All at Once', desc: 'Call all users simultaneously' },
    { id: 'staggered', title: 'Staggered', desc: 'Call in batches over time' },
  ];

  return (
    <div className="mx-auto max-w-3xl rounded-2xl border border-white/10 p-6 shadow-[0_10px_60px_-20px_rgba(0,255,163,0.18)] backdrop-blur-xl sm:p-8">
      {/* Header */}
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-white">Schedule Campaign</h2>
        <p className="mt-1 text-sm text-white/50">
          Configure when and how to send the vishing calls.
        </p>
      </div>

      {/* When to Send */}
      <div className="space-y-3">
        <Label className="text-sm text-white/80">When to Call</Label>
        <div className="grid gap-3 sm:grid-cols-2">
          {whenOptions.map(o => {
            const selected = when === o.id;
            const Icon = o.icon;
            return (
              <button
                key={o.id}
                type="button"
                onClick={() => setWhen(o.id)}
                className={cn(
                  'group relative rounded-xl border p-4 text-left transition-all duration-200 hover:-translate-y-0.5',
                  selected
                    ? 'border-[#00FFA3]/70 bg-[#00FFA3]/[0.06] shadow-[0_0_24px_rgba(0,255,163,0.25)]'
                    : 'border-white/10 bg-white/[0.02] hover:border-white/20 hover:bg-white/[0.04]',
                )}
              >
                <div className="flex items-start gap-3">
                  <span
                    className={cn(
                      'mt-0.5 grid h-4 w-4 shrink-0 place-items-center rounded-full border transition-colors',
                      selected ? 'border-[#00FFA3]' : 'border-white/30',
                    )}
                  >
                    {selected && (
                      <span className="size-2 rounded-full bg-[#00FFA3] shadow-[0_0_8px_rgba(0,255,163,0.8)]" />
                    )}
                  </span>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <Icon
                        className={cn(
                          'h-3.5 w-3.5',
                          selected ? 'text-[#00FFA3]' : 'text-white/50',
                        )}
                      />
                      <div className="text-sm font-medium text-white">
                        {o.title}
                      </div>
                    </div>
                    <div className="mt-1 text-xs leading-relaxed text-white/50">
                      {o.desc}
                    </div>
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Conditional Fields */}
      {when === 'later' && (
        <div className="mt-6 grid gap-4 rounded-xl border border-white/10 p-4 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="start-dt" className="text-sm text-white/80">
              Start Date &amp; Time <span className="text-[#00FFA3]">*</span>
            </Label>
            <Input
              id="start-dt"
              type="datetime-local"
              value={startDateTime}
              onChange={e => setStartDateTime(e.target.value)}
              placeholder="mm/dd/yyyy --:--"
            />
          </div>
          <div className="space-y-2">
            <Label className="text-sm text-white/80">Timezone</Label>
            <SearchSelect
              items={timezoneOptions.map(item => ({
                value: item.id,
                label: `${item.displayName} (${item.stateName} - ${item.timezoneId})`,
              }))}
              value={
                timezoneOptions.some(option => option.id === timezone)
                  ? timezone
                  : undefined
              }
              onValueChange={setTimezone}
              placeholder="Select Timezone"
            />
          </div>
        </div>
      )}

      {/* Sending Pattern */}
      <div className="mt-6 space-y-3">
        <Label className="text-sm text-white/80">Call Pattern</Label>
        <div className="grid gap-3 sm:grid-cols-2">
          {patternOptions.map(o => {
            const selected = pattern === o.id;
            return (
              <button
                key={o.id}
                type="button"
                onClick={() => setPattern(o.id)}
                className={cn(
                  'group relative rounded-xl border p-4 text-left transition-all duration-200 hover:-translate-y-0.5',
                  selected
                    ? 'border-[#00FFA3]/70 bg-[#00FFA3]/[0.06] shadow-[0_0_24px_rgba(0,255,163,0.25)]'
                    : 'border-white/10 bg-white/[0.02] hover:border-white/20 hover:bg-white/[0.04]',
                )}
              >
                <div className="flex items-start gap-3">
                  <span
                    className={cn(
                      'mt-0.5 grid h-4 w-4 shrink-0 place-items-center rounded-full border transition-colors',
                      selected ? 'border-[#00FFA3]' : 'border-white/30',
                    )}
                  >
                    {selected && (
                      <span className="size-2 rounded-full bg-[#00FFA3] shadow-[0_0_8px_rgba(0,255,163,0.8)]" />
                    )}
                  </span>
                  <div className="min-w-0">
                    <div className="text-sm font-medium text-white">
                      {o.title}
                    </div>
                    <div className="mt-1 text-xs leading-relaxed text-white/50">
                      {o.desc}
                    </div>
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}

// ---------- Step 5: Campaign Tags ----------
export function TagsTab() {
  const { setTags, tags: contextTags } = useVishingWizard();
  const { getAvailableTags } = useVishingCampaigns();
  const [tags, setTagsState] = useState<string[]>(() => contextTags.tags ?? []);
  const [input, setInput] = useState('');
  const [suggested, setSuggested] = useState<string[]>([
    'Q1 Campaign',
    'Training',
    'Compliance',
    'Executive',
    'IT Department',
    'Finance',
    'HR',
    'Sales',
    'Urgent',
    'Spear Phishing',
    'Credential Harvest',
  ]);

  useEffect(() => {
    const loadTags = async () => {
      const apiTags = await getAvailableTags();
      if (apiTags.length > 0) setSuggested(apiTags);
    };

    void loadTags();
  }, [getAvailableTags]);

  useEffect(() => {
    setTags({ tags });
  }, [tags, setTags]);

  const addTag = (raw: string) => {
    const t = raw.trim().replace(/^,+|,+$/g, '');
    if (!t) return;
    if (tags.includes(t)) return;
    if (tags.length >= 10) return;
    setTagsState(prev => [...prev, t]);
  };

  const removeTag = (t: string) =>
    setTagsState(prev => prev.filter(x => x !== t));

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      addTag(input);
      setInput('');
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (val.includes(',')) {
      const parts = val.split(',');
      parts.slice(0, -1).forEach(p => addTag(p));
      setInput(parts[parts.length - 1]);
    } else {
      setInput(val);
    }
  };

  return (
    <div className="mx-auto max-w-2xl rounded-2xl border border-white/10 p-6 shadow-[0_10px_60px_-20px_rgba(0,255,163,0.18)] backdrop-blur-xl sm:p-8">
      {/* Header */}
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-white">Campaign Tags</h2>
        <p className="mt-1 text-sm text-white/50">
          Add tags to categorize and organize your campaign (optional).
        </p>
      </div>

      {/* Selected tags */}
      <div className="mb-5 space-y-2">
        <Label className="text-sm text-white/80">Tags (max 10)</Label>
        <div
          className={cn(
            'min-h-[48px] rounded-xl border border-white/10 p-3 transition-colors',
            tags.length > 0 ? 'flex flex-wrap gap-2' : 'flex items-center',
          )}
        >
          {tags.length === 0 ? (
            <span className="text-sm text-white/30">No tags added yet.</span>
          ) : (
            tags.map(t => (
              <span
                key={t}
                className="inline-flex items-center gap-1.5 rounded-full border border-[#00FFA3]/30 bg-[#00FFA3]/10 px-3 py-1 text-sm font-medium text-[#00FFA3] shadow-[0_0_12px_rgba(0,255,163,0.15)] transition-all hover:shadow-[0_0_18px_rgba(0,255,163,0.35)]"
              >
                {t}
                <button
                  type="button"
                  onClick={() => removeTag(t)}
                  className="ml-0.5 inline-flex size-4 items-center justify-center rounded-full text-[#00FFA3]/70 transition-colors hover:bg-[#00FFA3]/20 hover:text-[#00FFA3]"
                  aria-label={`Remove ${t}`}
                >
                  <X className="size-3" />
                </button>
              </span>
            ))
          )}
        </div>
      </div>

      {/* Input */}
      <div className="mb-6 space-y-2">
        <Input
          value={input}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          placeholder="Type and press Enter to add..."
          disabled={tags.length >= 10}
        />
        <p className="text-xs text-white/40">
          Press Enter or comma to add a tag. {tags.length}/10 tags used.
        </p>
      </div>

      {/* Suggested tags */}
      <div className="space-y-3">
        <Label className="text-sm text-white/80">Suggested Tags</Label>
        <div className="flex flex-wrap gap-2">
          {suggested.map(s => {
            const selected = tags.includes(s);
            return (
              <button
                key={s}
                type="button"
                disabled={selected || tags.length >= 10}
                onClick={() => addTag(s)}
                className={cn(
                  'inline-flex items-center gap-1.5 rounded-full border px-3 py-1.5 text-sm transition-all duration-200',
                  selected
                    ? 'cursor-default border-[#00FFA3]/40 bg-[#00FFA3]/15 text-[#00FFA3]'
                    : 'border-white/15 bg-white/[0.04] text-white/70 hover:-translate-y-0.5 hover:border-[#00FFA3]/40 hover:bg-[#00FFA3]/10 hover:text-[#00FFA3] hover:shadow-[0_0_16px_rgba(0,255,163,0.25)] disabled:pointer-events-none disabled:opacity-40',
                )}
              >
                <Plus className="size-3.5" />
                {s}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}

export { TrainingBundleTab } from './TrainingBundleTab';

const withoutFileExtension = (fileName: string): string =>
  fileName.replace(/\.[^/.]+$/, '');

export function ReviewTab({
  setup,
  onEdit,
}: {
  setup: CampaignSetup;
  onEdit: (key: StepKey) => void;
}) {
  const {
    campaignId,
    voiceSetup,
    selectedScenarioId,
    scenarioAttach,
    telephony,
    tags,
    audience,
    training,
    schedule,
  } = useVishingWizard();
  const apiClient = useAPI();
  const { getCampaignById } = useVishingCampaigns();
  const { getScenarioById } = useVishingScenarios();
  const { getConfigurationById } = useVoiceServerConfigurations();
  const [campaign, setCampaign] = useState<IVishingCampaign | null>(null);
  const [scenario, setScenario] = useState<IVishingScenario | null>(null);
  const [voiceServer, setVoiceServer] =
    useState<IVoiceServerConfiguration | null>(null);
  const [timezoneOptions, setTimezoneOptions] = useState<ITimezoneOption[]>([]);

  useEffect(() => {
    let cancelled = false;
    const loadTimezones = async () => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.GET_ACTIVE_TIME_ZONE_LIST,
        );
        if (cancelled) return;
        const timezoneData = (response?.data?.data ??
          response?.data ??
          []) as ITimezoneOption[];
        setTimezoneOptions(timezoneData);
      } catch (error) {
        console.error('Error fetching timezone options:', error);
      }
    };
    void loadTimezones();
    return () => {
      cancelled = true;
    };
  }, [apiClient]);

  useEffect(() => {
    if (!campaignId) {
      setCampaign(null);
      return;
    }

    let cancelled = false;
    void getCampaignById(campaignId).then(result => {
      if (!cancelled) setCampaign(result);
    });
    return () => {
      cancelled = true;
    };
  }, [campaignId, getCampaignById]);

  useEffect(() => {
    if (!selectedScenarioId) {
      setScenario(null);
      return;
    }

    let cancelled = false;
    void getScenarioById(selectedScenarioId).then(result => {
      if (!cancelled) setScenario(result);
    });
    return () => {
      cancelled = true;
    };
  }, [selectedScenarioId, getScenarioById]);

  useEffect(() => {
    if (!telephony.voiceServerConfigurationId) {
      setVoiceServer(null);
      return;
    }

    let cancelled = false;
    void getConfigurationById(telephony.voiceServerConfigurationId).then(
      result => {
        if (!cancelled) setVoiceServer(result);
      },
    );
    return () => {
      cancelled = true;
    };
  }, [telephony.voiceServerConfigurationId, getConfigurationById]);

  const activityPeriod = formatCampaignExpireSummary(setup.expireDate) ?? '—';
  const voiceFileName =
    campaign?.voiceData?.fileName ||
    voiceSetup.fileName ||
    voiceSetup.audioFile?.name;
  const voiceLabel = voiceFileName
    ? withoutFileExtension(voiceFileName)
    : voiceSetup.voiceName || campaign?.voiceData?.voiceName || '—';
  const scenarioLabel = scenario?.scenarioName || selectedScenarioId || '—';
  const providerLabel =
    voiceServer?.name || telephony.voiceServerConfigurationId || '—';
  const callerId = voiceSetup.callerId || voiceServer?.callerId || '—';
  const region =
    voiceServer?.region ||
    telephony.region ||
    voiceServer?.countryCode ||
    telephony.countryCode ||
    '—';
  const recipientCount =
    audience.audienceType === 'INDIVIDUAL'
      ? (audience.userIds?.length ?? 0)
      : null;
  const audienceLabel =
    audience.audienceType === 'ALL_USERS'
      ? 'All users'
      : audience.audienceType === 'DEPARTMENTS'
        ? `${audience.departmentIds?.length ?? 0} department(s)`
        : audience.audienceType === 'GROUPS'
          ? `${audience.groupIds?.length ?? 0} group(s)`
          : audience.audienceType === 'RISK_GROUPS'
            ? `${audience.riskGroups?.length ?? 0} risk group(s)`
            : 'Individual selection';
  const trainingModuleCount =
    training.topicIdDetails?.length ??
    training.topicId?.length ??
    (training.trainingModuleId ? 1 : 0);
  const trainingLabel =
    training.subPackageName ||
    campaign?.trainingData?.subPackageName ||
    training.name ||
    campaign?.trainingData?.name ||
    training.productPackageId ||
    campaign?.trainingData?.productPackageId ||
    training.packageId ||
    campaign?.trainingData?.packageId ||
    '—';
  const reviewTags = tags.tags?.length ? tags.tags : (campaign?.tags ?? []);
  const scheduleUi = scheduleToUiState(schedule);
  const scheduleLabel =
    scheduleUi.when === 'immediate'
      ? 'Send Immediately'
      : scheduleUi.when === 'recurring'
        ? 'Recurring'
        : 'Schedule for Later';
  const schedulePattern =
    scheduleUi.pattern === 'staggered' ? 'Staggered' : 'All at once';
  const resolvedTimezoneId = resolveTimezoneId(
    scheduleUi.timezone,
    timezoneOptions,
  );
  const timezoneOption = timezoneOptions.find(
    option => option.id === resolvedTimezoneId,
  );
  const timezoneLabel = timezoneOption
    ? `${timezoneOption.displayName} (${timezoneOption.timezoneId})`
    : scheduleUi.timezone || '—';

  const sections: {
    id: string;
    step: StepKey;
    title: string;
    icon: typeof Mail;
    rows: { label: string; value: React.ReactNode; highlight?: boolean }[];
  }[] = [
    {
      id: 'setup',
      step: 'setup',
      title: 'Campaign Setup',
      icon: Settings2,
      rows: [
        { label: 'Name', value: setup.name || '—', highlight: true },
        {
          label: 'Type',
          value:
            setup.type === 'VISHING_SIMULATION'
              ? 'AI Vishing Simulation'
              : 'AI Vishing with Training',
        },
        {
          label: 'Response stages',
          value:
            setup.stages.length > 0
              ? setup.stages
                  .map(s =>
                    s === 'CALL_ENGAGED' ? 'Call Engaged' : 'Compromised',
                  )
                  .join(', ')
              : '—',
        },
        ...(setup.type === 'VISHING_WITH_TRAINING'
          ? [
              {
                label: 'Learning mode',
                value: setup.learningMode.replace(/_/g, ' '),
              },
            ]
          : []),
        { label: 'Activity period', value: activityPeriod },
      ],
    },
    {
      id: 'identity',
      step: 'voice',
      title: 'Identity & Voice',
      icon: Mic,
      rows: [
        { label: 'Voice', value: voiceLabel, highlight: true },
        {
          label: 'Engine',
          value: voiceSetup.cloningEngine?.replace(/_/g, ' ') ?? '—',
        },
        { label: 'Language', value: voiceSetup.language ?? '—' },
        { label: 'Caller ID', value: callerId },
      ],
    },
    {
      id: 'scenario',
      step: 'scenario',
      title: 'Scenario & Script',
      icon: FileText,
      rows: [
        {
          label: 'Scenario',
          value: scenarioLabel,
          highlight: true,
        },
        {
          label: 'Template',
          value: scenario?.attackTemplateName ?? 'Custom',
        },
        {
          label: 'LLM responses',
          value: scenarioAttach.enableLlmResponses ? 'Enabled' : 'Disabled',
        },
        {
          label: 'Escalation',
          value: scenarioAttach.escalationLimit ?? '—',
        },
      ],
    },
    {
      id: 'telephony',
      step: 'telephony',
      title: 'SIP / VoIP Provider',
      icon: PhoneForwarded,
      rows: [
        {
          label: 'Provider',
          value: providerLabel,
          highlight: true,
        },
        { label: 'Region', value: region },
        {
          label: 'Retries',
          value: String(telephony.retryPolicy?.maxRetries ?? 0),
        },
        {
          label: 'Retry interval',
          value: `${telephony.retryPolicy?.intervalMinutes ?? 0} minutes`,
        },
      ],
    },
    {
      id: 'tags',
      step: 'tags',
      title: 'Tags',
      icon: Sparkles,
      rows: [
        {
          label: 'Labels',
          value: (
            <div className="flex flex-wrap justify-end gap-1.5">
              {reviewTags.length ? (
                reviewTags.map(t => (
                  <span
                    key={t}
                    className="rounded-full border border-[#00FFA3]/30 bg-[#00FFA3]/10 px-2.5 py-0.5 text-[11px] font-medium text-[#00FFA3]"
                  >
                    {t}
                  </span>
                ))
              ) : (
                <span>—</span>
              )}
            </div>
          ),
        },
      ],
    },
    {
      id: 'audience',
      step: 'audience',
      title: 'Audience',
      icon: Users,
      rows: [
        { label: 'Type', value: audienceLabel, highlight: true },
        {
          label: 'Recipients',
          value:
            recipientCount == null
              ? 'Determined by audience'
              : `${recipientCount} recipient${recipientCount === 1 ? '' : 's'}`,
        },
      ],
    },
    ...(setup.type === 'VISHING_WITH_TRAINING'
      ? [
          {
            id: 'training',
            step: 'training' as StepKey,
            title: 'Training & Content',
            icon: BookOpen,
            rows: [
              {
                label: 'Package',
                value: trainingLabel,
                highlight: true,
              },
              {
                label: 'Modules',
                value: `${trainingModuleCount} selected`,
              },
            ],
          },
        ]
      : []),
    {
      id: 'schedule',
      step: 'schedule',
      title: 'Schedule',
      icon: Calendar,
      rows: [
        { label: 'When', value: scheduleLabel, highlight: true },
        ...(scheduleUi.startDateTime
          ? [{ label: 'Starts', value: scheduleUi.startDateTime }]
          : []),
        { label: 'Timezone', value: timezoneLabel },
        { label: 'Pattern', value: schedulePattern },
      ],
    },
  ];

  return (
    <div className="mx-auto max-w-5xl">
      {/* Header */}
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-white sm:text-2xl">
          Review Campaign
        </h2>
        <p className="mt-1 text-sm text-white/50">
          Review your campaign settings before launching.
        </p>
      </div>

      {/* Grid */}
      <div className="grid gap-4 sm:grid-cols-2">
        {sections.map(s => {
          const Icon = s.icon;
          return (
            <div
              key={s.id}
              className="group relative rounded-2xl border border-white/10 bg-white/[0.03] p-5 backdrop-blur-xl transition-all duration-200 hover:-translate-y-0.5 hover:border-[#00FFA3]/30 hover:shadow-[0_10px_40px_-12px_rgba(0,255,163,0.25)]"
            >
              <div className="mb-3 flex items-start justify-between gap-3">
                <div className="flex items-center gap-2.5">
                  <div className="grid size-8 place-items-center rounded-lg border border-white/10 bg-white/[0.04] text-[#00FFA3]">
                    <Icon className="size-4" />
                  </div>
                  <h3 className="text-sm font-semibold text-white">
                    {s.title}
                  </h3>
                </div>
                <button
                  type="button"
                  onClick={() => onEdit(s.step)}
                  className="inline-flex items-center gap-1 rounded-md border border-white/10 bg-white/[0.04] px-2 py-1 text-[11px] font-medium text-white/70 transition-colors hover:border-[#00FFA3]/40 hover:bg-[#00FFA3]/10 hover:text-[#00FFA3]"
                >
                  <Pencil className="size-3" /> Edit
                </button>
              </div>
              <dl className="space-y-2 text-sm">
                {s.rows.map((row, i) => (
                  <div
                    key={i}
                    className="flex items-start justify-between gap-3 border-b border-white/5 pb-2 last:border-0 last:pb-0"
                  >
                    <dt className="text-xs uppercase tracking-wide text-white/40">
                      {row.label}
                    </dt>
                    <dd
                      className={
                        row.highlight
                          ? 'text-right text-sm font-medium text-[#00FFA3]'
                          : 'text-right text-sm text-white/85'
                      }
                    >
                      {row.value}
                    </dd>
                  </div>
                ))}
              </dl>
            </div>
          );
        })}
      </div>

      {/* Warning */}
      <div className="mt-6 flex items-start gap-3 rounded-2xl border border-amber-400/30 bg-amber-400/[0.06] p-4 backdrop-blur-xl">
        <div className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-lg bg-amber-400/15 text-amber-300">
          <AlertCircle className="size-4" />
        </div>
        <div className="text-sm">
          <div className="font-semibold text-amber-200">Before you launch</div>
          <p className="mt-0.5 text-amber-100/70">
            Once launched, calls will be placed to{' '}
            {recipientCount == null ? (
              <span className="font-semibold text-amber-200">
                the selected audience
              </span>
            ) : (
              <>
                <span className="font-semibold text-amber-200">
                  {recipientCount}
                </span>{' '}
                recipient{recipientCount === 1 ? '' : 's'}
              </>
            )}
            . Make sure all settings are correct.
          </p>
        </div>
      </div>
    </div>
  );
}
