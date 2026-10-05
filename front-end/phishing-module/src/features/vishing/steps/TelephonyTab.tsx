import { useEffect, useMemo, useState } from 'react';
import {
  AlertCircle,
  Check,
  Loader2,
  MapPin,
  PhoneCall,
  Play,
  Repeat,
  Settings2,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Button } from 'components/common/Button';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import { cn } from 'utils/Helper';
import { toast } from 'react-toastify';
import { Link } from 'react-router-dom';
import { useVishingWizard } from '../context/VishingWizardContext';
import { useVoiceServerConfigurations } from 'hooks/UseVoiceServerConfigurations';
import {
  IVoiceServerConfiguration,
  resolveVoiceProviderLabel,
  VOICE_COUNTRY_OPTIONS,
  VoiceServerConfigurationStatus,
} from 'models/VoiceServerConfiguration';
import { routes } from 'routes/Routes';

type RichProvider = {
  id: string;
  name: string;
  providerLabel: string;
  status: 'connected' | 'disconnected';
  country: string;
  countryLabel: string;
  region?: string;
  callerId?: string;
  isDefault?: boolean;
  isActive: boolean;
};

const COUNTRY_LABELS: Record<string, string> = Object.fromEntries(
  VOICE_COUNTRY_OPTIONS.map(option => [option.value, option.label]),
);

const ALL_COUNTRIES = 'ALL';

const mapVoiceServerToProvider = (
  config: IVoiceServerConfiguration,
): RichProvider => {
  const country = (config.countryCode || 'US').toUpperCase();
  return {
    id: config.id,
    name: config.name,
    providerLabel: resolveVoiceProviderLabel(config.provider),
    status:
      config.status === VoiceServerConfigurationStatus.ACTIVE
        ? 'connected'
        : 'disconnected',
    country,
    countryLabel: COUNTRY_LABELS[country] ?? country,
    region: config.region,
    callerId: config.callerId,
    isDefault: Boolean(config.default),
    isActive: config.status === VoiceServerConfigurationStatus.ACTIVE,
  };
};

const DEFAULT_TEST_PHONE = '+19176009233';

const sanitizePhoneNumber = (value: string): string => {
  const digitsAndPlus = value.replace(/[^\d+]/g, '');
  if (!digitsAndPlus) return '';

  const hasLeadingPlus = digitsAndPlus.startsWith('+');
  const digits = digitsAndPlus.replace(/\D/g, '');
  return hasLeadingPlus ? `+${digits}` : digits;
};

const isValidPhoneNumber = (value: string): boolean =>
  /^\+?[1-9]\d{7,14}$/.test(value.trim());

export function TelephonyTab() {
  const { setTelephony, telephony } = useVishingWizard();
  const { fetchConfigurations, testConfiguration, loading } =
    useVoiceServerConfigurations();

  const [apiProviders, setApiProviders] = useState<RichProvider[]>([]);
  const [userCountry, setUserCountry] = useState<string>(() =>
    telephony.countryCode ? telephony.countryCode.toUpperCase() : ALL_COUNTRIES,
  );
  const [selectedId, setSelectedId] = useState<string | null>(
    () => telephony.voiceServerConfigurationId || null,
  );
  const [testing, setTesting] = useState(false);
  const [testPhoneNumber, setTestPhoneNumber] = useState('');
  const [retries, setRetries] = useState(
    () => telephony.retryPolicy?.maxRetries ?? 3,
  );
  const [interval, setIntervalMinutes] = useState(
    () => telephony.retryPolicy?.intervalMinutes ?? 15,
  );
  const [hasLoaded, setHasLoaded] = useState(false);

  const countryOptions = useMemo(() => {
    const fromApi = Array.from(
      new Set(apiProviders.map(provider => provider.country).filter(Boolean)),
    ).sort();
    const known = VOICE_COUNTRY_OPTIONS.map(option => option.value);
    const merged = Array.from(new Set([...known, ...fromApi]));
    return merged.map(code => ({
      value: code,
      label: COUNTRY_LABELS[code] ?? code,
    }));
  }, [apiProviders]);

  const providers = useMemo(() => {
    const active = apiProviders.filter(provider => provider.isActive);
    if (userCountry === ALL_COUNTRIES) return active;
    return active.filter(provider => provider.country === userCountry);
  }, [apiProviders, userCountry]);

  useEffect(() => {
    const loadProviders = async () => {
      const result = await fetchConfigurations({ pageSize: 100, offset: 0 });
      const mapped = (result.items || []).map(mapVoiceServerToProvider);
      setApiProviders(mapped);
      setHasLoaded(true);

      setSelectedId(prev => {
        if (prev && mapped.some(item => item.id === prev && item.isActive)) {
          return prev;
        }
        const defaultProvider = mapped.find(
          item => item.isDefault && item.isActive,
        );
        return defaultProvider?.id ?? prev ?? null;
      });
    };

    void loadProviders();
  }, [fetchConfigurations]);

  useEffect(() => {
    if (!selectedId) {
      setTelephony({
        voiceServerConfigurationId: '',
        retryPolicy: {
          maxRetries: retries,
          intervalMinutes: interval,
        },
      });
      return;
    }

    const provider =
      providers.find(item => item.id === selectedId) ||
      apiProviders.find(item => item.id === selectedId);

    setTelephony({
      voiceServerConfigurationId: selectedId,
      region: provider?.region,
      countryCode: provider?.country,
      retryPolicy: {
        maxRetries: retries,
        intervalMinutes: interval,
      },
    });
  }, [selectedId, providers, apiProviders, retries, interval, setTelephony]);

  const runTest = async () => {
    if (!selectedId) return;
    const phoneNumber = testPhoneNumber.trim();
    if (!isValidPhoneNumber(phoneNumber)) {
      toast.error(
        'Enter a valid phone number (8–15 digits, optional leading +).',
      );
      return;
    }
    setTesting(true);
    try {
      const result = await testConfiguration(selectedId, {
        phoneNumber,
      });

      if (result?.success) {
        toast.success(result.message || 'Test call completed successfully.');
      } else {
        toast.error(result?.message || 'Test call failed.');
      }
    } finally {
      setTesting(false);
    }
  };

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <CardHeader>
          <div className="mb-5 flex items-start justify-between gap-3">
            <div>
              <div className="flex items-center gap-2">
                <CardTitle className="flex items-center gap-2">
                  <Settings2 className="size-3.5 text-[#00FFA3]" />
                  Voice Server Providers
                </CardTitle>
              </div>
              <p className="mt-1 text-sm text-white/55">
                Select an active voice server configuration for outbound calls.
              </p>
            </div>
            <Select
              value={userCountry}
              onValueChange={value => {
                setUserCountry(value);
                setSelectedId(current => {
                  if (!current) return null;
                  if (value === ALL_COUNTRIES) return current;
                  const stillVisible = apiProviders.some(
                    item =>
                      item.id === current &&
                      item.isActive &&
                      item.country === value,
                  );
                  return stillVisible ? current : null;
                });
              }}
            >
              <SelectTrigger className="h-8 w-[160px] border-white/10 bg-white/5 text-xs text-white/80">
                <MapPin className="mr-1 size-3 text-[#00FFA3]" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL_COUNTRIES}>All countries</SelectItem>
                {countryOptions.map(option => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardHeader>

        <CardContent>
          <div className="space-y-2.5">
            {loading && !hasLoaded ? (
              <div className="flex items-center justify-center gap-2 rounded-xl border border-white/10 bg-white/[0.02] p-8 text-sm text-white/50">
                <Loader2 className="size-4 animate-spin" />
                Loading voice servers…
              </div>
            ) : (
              providers.map(provider => {
                const isSelected = selectedId === provider.id;
                const isConnected = provider.status === 'connected';
                return (
                  <button
                    key={provider.id}
                    type="button"
                    onClick={() => setSelectedId(provider.id)}
                    className={cn(
                      'group relative w-full overflow-hidden rounded-xl border p-3.5 text-left transition-all duration-200',
                      'backdrop-blur-md',
                      isSelected
                        ? 'border-[#00FFA3]/60 bg-[#00FFA3]/[0.06] shadow-[0_0_0_1px_rgba(0,255,163,0.35),0_8px_30px_-12px_rgba(0,255,163,0.45)]'
                        : 'border-white/10 bg-white/[0.03] hover:-translate-y-0.5 hover:border-white/20 hover:bg-white/[0.05] hover:shadow-[0_8px_30px_-15px_rgba(0,255,163,0.35)]',
                    )}
                  >
                    <div className="flex items-center justify-between gap-3">
                      <div className="min-w-0 flex-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="truncate text-sm font-medium text-white">
                            {provider.name}
                          </span>
                          <span className="text-[11px] text-white/40">
                            ({provider.countryLabel})
                          </span>
                          {provider.isDefault ? (
                            <span className="rounded-full border border-[#00FFA3]/25 bg-[#00FFA3]/[0.08] px-1.5 py-px text-[10px] font-medium text-[#00FFA3]">
                              Default
                            </span>
                          ) : null}
                        </div>
                        <div className="mt-1 flex items-center gap-2 font-mono text-[11px] text-white/45">
                          <span className="truncate">
                            {provider.providerLabel}
                          </span>
                          {provider.callerId ? (
                            <>
                              <span className="text-white/20">·</span>
                              <span>{provider.callerId}</span>
                            </>
                          ) : null}
                          {provider.region ? (
                            <>
                              <span className="text-white/20">·</span>
                              <span>{provider.region}</span>
                            </>
                          ) : null}
                        </div>
                      </div>
                      <div className="flex shrink-0 items-center gap-2">
                        <span
                          className={cn(
                            'inline-flex items-center gap-1.5 rounded-full px-2 py-0.5 text-[10px] font-medium',
                            isConnected
                              ? 'bg-[#00FFA3]/10 text-[#00FFA3] ring-1 ring-[#00FFA3]/30'
                              : 'bg-white/5 text-white/45 ring-1 ring-white/10',
                          )}
                        >
                          <span
                            className={cn(
                              'h-1.5 w-1.5 rounded-full',
                              isConnected
                                ? 'bg-[#00FFA3] shadow-[0_0_6px_#00FFA3]'
                                : 'bg-white/40',
                            )}
                          />
                          {isConnected ? 'active' : 'inactive'}
                        </span>
                        <span
                          className={cn(
                            'flex h-6 w-6 items-center justify-center rounded-full transition-all',
                            isSelected
                              ? 'bg-[#00FFA3] text-black'
                              : 'bg-white/5 text-transparent ring-1 ring-white/10 group-hover:text-white/30',
                          )}
                        >
                          <Check className="size-3.5" />
                        </span>
                      </div>
                    </div>

                    {isSelected && provider.isDefault ? (
                      <div className="mt-3 flex items-start gap-2.5 rounded-lg border border-amber-400/30 bg-amber-400/[0.07] p-2.5 text-[12px] text-amber-200/90 shadow-[0_0_20px_-8px_rgba(251,191,36,0.45)]">
                        <AlertCircle className="mt-0.5 size-4 shrink-0 text-amber-300" />
                        <p className="leading-snug">
                          This is your default voice server. You can keep it or
                          choose another active configuration.
                        </p>
                      </div>
                    ) : null}
                  </button>
                );
              })
            )}

            {hasLoaded && !loading && providers.length === 0 ? (
              <div className="rounded-xl border border-dashed border-white/10 bg-white/[0.02] p-6 text-center text-sm text-white/50">
                <p>
                  {apiProviders.length === 0
                    ? 'No voice server configurations found.'
                    : 'No active voice servers for this country filter.'}
                </p>
                <Link
                  to={routes.voiceServerConfiguration.path}
                  className="mt-3 inline-flex text-[#00FFA3] hover:underline"
                >
                  Manage voice server configurations
                </Link>
              </div>
            ) : null}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Play className="size-4 text-primary" /> Browser Test Mode
          </CardTitle>
          <CardDescription>
            Simulate an outbound call without targeting real users.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="rounded-lg border border-dashed border-card-border bg-muted/20 p-6 text-center">
            <PhoneCall
              className={`mx-auto size-10 ${testing ? 'animate-pulse text-primary' : 'text-muted-foreground'}`}
            />
            <p className="mt-2 text-sm">
              {testing
                ? 'Running simulated test call…'
                : selectedId
                  ? 'Ready to start test call'
                  : 'Select a telephony server to enable test calls'}
            </p>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="test-phone">Test phone number</Label>
            <Input
              id="test-phone"
              type="tel"
              inputMode="tel"
              autoComplete="tel"
              value={testPhoneNumber}
              onChange={e =>
                setTestPhoneNumber(sanitizePhoneNumber(e.target.value))
              }
              placeholder={DEFAULT_TEST_PHONE}
              disabled={!selectedId || testing}
            />
            {testPhoneNumber.trim() &&
              !isValidPhoneNumber(testPhoneNumber.trim()) && (
                <p className="text-xs text-destructive">
                  Enter a valid phone number (8–15 digits, optional leading +).
                </p>
              )}
          </div>
          <div className="flex gap-2">
            <Button
              onClick={() => void runTest()}
              disabled={
                !selectedId ||
                testing ||
                !isValidPhoneNumber(testPhoneNumber.trim())
              }
              className="flex-1"
            >
              <Play className="mr-1 size-4" /> Start test
            </Button>
          </div>
          <p className="text-xs text-muted-foreground">
            Test mode is fully isolated — calls never reach production targets.
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Repeat className="size-4 text-primary" /> Retry Policy
          </CardTitle>
          <CardDescription>
            Automatic retries for unanswered calls (max 3).
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="space-y-1.5">
            <Label>Max retries: {retries}</Label>
            <Input
              type="number"
              min={0}
              max={3}
              value={retries}
              onChange={e =>
                setRetries(Math.min(Math.max(Number(e.target.value), 0), 3))
              }
            />
          </div>
          <div className="space-y-1.5">
            <Label>Interval (minutes)</Label>
            <Input
              type="number"
              min={1}
              value={interval}
              onChange={e => setIntervalMinutes(Number(e.target.value))}
            />
          </div>
          <Button
            onClick={() => toast.success('Retry policy saved.')}
            className="w-full"
          >
            Save retry policy
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
