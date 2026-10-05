import { Button } from 'common/Button';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  IBreachConfig,
  IBreachConfigRequest,
  IBreachSyncResult,
} from 'models/Breach';
import { useEffect, useState } from 'react';

interface IBreachSettingsFormProps {
  config: IBreachConfig | null;
  loading: boolean;
  saving: boolean;
  syncing: boolean;
  lastSyncResult: IBreachSyncResult | null;
  onSave: (config: IBreachConfigRequest) => void;
  onSync: () => void;
}

/**
 * Form component for breach detection settings
 */
const BreachSettingsForm = ({
  config,
  loading,
  saving,
  syncing,
  lastSyncResult,
  onSave,
  onSync,
}: IBreachSettingsFormProps) => {
  const [collectBreachData, setCollectBreachData] = useState(true);
  const [monitoredDomains, setMonitoredDomains] = useState<string[]>([]);
  const [newDomain, setNewDomain] = useState('');
  const [autoNotifyUsers, setAutoNotifyUsers] = useState(false);
  const [requirePasswordReset, setRequirePasswordReset] = useState(false);
  const [syncIntervalHours, setSyncIntervalHours] = useState(6);

  useEffect(() => {
    if (config) {
      setTimeout(() => {
        setCollectBreachData(config.collectBreachData);
        setMonitoredDomains(config.monitoredDomains || []);
        setAutoNotifyUsers(config.autoNotifyUsers);
        setRequirePasswordReset(config.requirePasswordReset);
        setSyncIntervalHours(config.syncIntervalHours || 6);
      }, 0);
    }
  }, [config]);

  const handleAddDomain = () => {
    const domain = newDomain.trim().toLowerCase();
    if (domain && !monitoredDomains.includes(domain)) {
      setMonitoredDomains([...monitoredDomains, domain]);
      setNewDomain('');
    }
  };

  const handleRemoveDomain = (domain: string) => {
    setMonitoredDomains(monitoredDomains.filter(d => d !== domain));
  };

  const handleSave = () => {
    onSave({
      collectBreachData,
      monitoredDomains,
      autoNotifyUsers,
      requirePasswordReset,
      syncIntervalHours,
    });
  };

  const formatDate = (dateString: string | null) => {
    if (!dateString) return 'Never';
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="animate-pulse space-y-4">
        <div className="h-8 w-1/3 rounded bg-gray-200"></div>
        <div className="h-20 rounded bg-gray-200"></div>
        <div className="h-20 rounded bg-gray-200"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Main Toggle */}
      <div className="rounded-lg border border-card-border p-6">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-medium text-foreground">
              Breach Data Collection
            </h3>
            <p className="text-sm text-muted-foreground">
              Enable or disable automatic collection of breach data from
              external sources
            </p>
          </div>
          <label className="relative inline-flex cursor-pointer items-center">
            <input
              type="checkbox"
              checked={collectBreachData}
              onChange={e => setCollectBreachData(e.target.checked)}
              className="peer sr-only"
            />
            <div className="peer h-6 w-11 rounded-full bg-muted-foreground/10 after:absolute after:left-[2px] after:top-[2px] after:size-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all after:content-[''] peer-checked:bg-blue-600 peer-checked:after:translate-x-full peer-checked:after:border-white peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300"></div>
          </label>
        </div>
      </div>

      {/* Monitored Domains */}
      <div className="rounded-lg border border-card-border p-6">
        <h3 className="mb-4 text-lg font-medium text-foreground">
          Monitored Domains
        </h3>
        <p className="mb-4 text-sm text-muted-foreground">
          Add domains to monitor for data breaches. These will be checked
          periodically.
        </p>

        {/* Add Domain */}
        <div className="mb-4 flex gap-2">
          <Input
            type="text"
            value={newDomain}
            onChange={e => setNewDomain(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleAddDomain()}
            placeholder="example.com"
          />
          <Button
            onClick={handleAddDomain}
            disabled={!newDomain.trim()}
            className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/80 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Add Domain
          </Button>
        </div>

        {/* Domain List */}
        <div className="space-y-2">
          {monitoredDomains.length === 0 ? (
            <p className="text-sm italic text-muted-foreground">
              No domains added yet
            </p>
          ) : (
            monitoredDomains.map(domain => (
              <div
                key={domain}
                className="flex items-center justify-between rounded-md px-3 py-2"
              >
                <span className="text-sm font-medium text-foreground">
                  {domain}
                </span>
                <button
                  onClick={() => handleRemoveDomain(domain)}
                  className="text-destructive hover:text-destructive/80"
                >
                  <svg
                    className="size-5"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M6 18L18 6M6 6l12 12"
                    />
                  </svg>
                </button>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Notification Settings */}
      <div className="rounded-lg border border-card-border p-6">
        <h3 className="mb-4 text-lg font-medium text-foreground">
          Notification Settings
        </h3>

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-foreground">
                Auto-notify Users
              </p>
              <p className="text-sm text-muted-foreground">
                Automatically send notifications to users when their data is
                found in a breach
              </p>
            </div>
            <label className="relative inline-flex cursor-pointer items-center">
              <input
                type="checkbox"
                checked={autoNotifyUsers}
                onChange={e => setAutoNotifyUsers(e.target.checked)}
                className="peer sr-only"
              />
              <div className="peer h-6 w-11 rounded-full bg-gray-200 after:absolute after:left-[2px] after:top-[2px] after:size-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all after:content-[''] peer-checked:bg-blue-600 peer-checked:after:translate-x-full peer-checked:after:border-white peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300"></div>
            </label>
          </div>

          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-foreground">
                Require Password Reset
              </p>
              <p className="text-sm text-muted-foreground">
                Force affected users to reset their passwords
              </p>
            </div>
            <label className="relative inline-flex cursor-pointer items-center">
              <input
                type="checkbox"
                checked={requirePasswordReset}
                onChange={e => setRequirePasswordReset(e.target.checked)}
                className="peer sr-only"
              />
              <div className="peer h-6 w-11 rounded-full bg-gray-200 after:absolute after:left-[2px] after:top-[2px] after:size-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all after:content-[''] peer-checked:bg-blue-600 peer-checked:after:translate-x-full peer-checked:after:border-white peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300"></div>
            </label>
          </div>
        </div>
      </div>

      {/* Sync Settings */}
      <div className="rounded-lg border border-card-border p-6">
        <h3 className="mb-4 text-lg font-medium text-foreground">
          Sync Settings
        </h3>

        <div className="grid grid-cols-2 gap-6">
          <div>
            <label className="mb-1 block text-sm font-medium text-foreground">
              Sync Interval (hours)
            </label>
            <Select
              value={syncIntervalHours.toString()}
              onValueChange={value => setSyncIntervalHours(Number(value))}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select sync interval" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="1">Every hour</SelectItem>
                <SelectItem value="6">Every 6 hours</SelectItem>
                <SelectItem value="12">Every 12 hours</SelectItem>
                <SelectItem value="24">Daily</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-foreground">
              Last Sync
            </label>
            <p className="py-2 text-sm text-muted-foreground">
              {formatDate(config?.lastSyncAt || null)}
            </p>
          </div>
        </div>

        <div className="mt-4">
          <button
            onClick={onSync}
            disabled={syncing || !collectBreachData}
            className="inline-flex items-center rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/80 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {syncing ? (
              <>
                <svg
                  className="-ml-1 mr-2 size-4 animate-spin"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                    fill="none"
                  />
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  />
                </svg>
                Syncing...
              </>
            ) : (
              <>
                <svg
                  className="mr-2 size-4"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                  />
                </svg>
                Sync Now
              </>
            )}
          </button>
        </div>

        {/* Last Sync Result */}
        {lastSyncResult && (
          <div className="mt-4 rounded-md p-4">
            <h4 className="mb-2 text-sm font-medium text-foreground">
              Last Sync Result
            </h4>
            <div className="grid grid-cols-4 gap-4 text-sm">
              <div>
                <span className="text-muted-foreground">Domains:</span>{' '}
                <span className="font-medium">
                  {lastSyncResult.domainsProcessed}
                </span>
              </div>
              <div>
                <span className="text-muted-foreground">New Breaches:</span>{' '}
                <span className="font-medium">
                  {lastSyncResult.newBreachesFound}
                </span>
              </div>
              <div>
                <span className="text-muted-foreground">New Recipients:</span>{' '}
                <span className="font-medium">
                  {lastSyncResult.newRecipientsFound}
                </span>
              </div>
              <div>
                <span className="text-muted-foreground">Errors:</span>{' '}
                <span
                  className={`font-medium ${
                    lastSyncResult.errorsEncountered > 0
                      ? 'text-destructive'
                      : ''
                  }`}
                >
                  {lastSyncResult.errorsEncountered}
                </span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Save Button */}
      <div className="flex justify-end">
        <button
          onClick={handleSave}
          disabled={saving}
          className="flex items-center gap-2 rounded-md bg-primary px-6 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/80 disabled:opacity-50"
        >
          {saving ? (
            <>
              <svg className="size-4 animate-spin" viewBox="0 0 24 24">
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                  fill="none"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                />
              </svg>
              Saving...
            </>
          ) : (
            'Save Settings'
          )}
        </button>
      </div>
    </div>
  );
};

export default BreachSettingsForm;
