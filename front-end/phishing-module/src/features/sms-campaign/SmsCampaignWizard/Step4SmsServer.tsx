import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import { useSmsServerConfigurations } from 'hooks/UseSmsServerConfigurations';
import useDebounce from 'hooks/UseDebounce';
import {
  ArrowLeftIcon,
  ArrowRightIcon,
  CheckIcon,
  RotateCcw,
  Search,
} from 'lucide-react';
import {
  ISmsServerConfiguration,
  getSmsProviderLabel,
  SmsServerConfigurationStatus,
} from 'models/SmsServerConfiguration';
import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { routes } from 'routes/Routes';
import { DEBOUNCE_DELAY } from 'utils/Constants';
import { cn } from 'utils/Helper';
import SmsServerConfigurationStatusBadge from 'features/sms-server-configuration/SmsServerConfigurationStatusBadge';

interface Step4SmsServerProps {
  selectedConfigurationId?: string;
  onSubmit: (configurationId: string, configurationName: string) => void;
  onBack: () => void;
  isLoading?: boolean;
}

const PAGE_SIZE = 12;

export const Step4SmsServer = ({
  selectedConfigurationId,
  onSubmit,
  onBack,
  isLoading,
}: Step4SmsServerProps) => {
  const { configurations, fetchConfigurations, loading } =
    useSmsServerConfigurations();

  const [selectedId, setSelectedId] = useState<string>(
    selectedConfigurationId || '',
  );
  const [searchValue, setSearchValue] = useState('');
  const debouncedSearch = useDebounce(searchValue, DEBOUNCE_DELAY);
  const [offset, setOffset] = useState(0);

  useEffect(() => {
    void fetchConfigurations({
      offset,
      pageSize: PAGE_SIZE,
      searchParam: debouncedSearch,
      sortBy: 'name',
      sortOrder: 'asc',
    });
  }, [fetchConfigurations, offset, debouncedSearch]);

  const activeItems =
    configurations.items?.filter(
      c => c.status === SmsServerConfigurationStatus.ACTIVE,
    ) ?? [];

  const selectedName =
    configurations.items?.find(c => c.id === selectedId)?.name ?? '';

  const handleReset = useCallback(() => {
    setSearchValue('');
    setOffset(0);
  }, []);

  const handleSubmit = () => {
    if (!selectedId) return;
    onSubmit(selectedId, selectedName);
  };

  const currentPage = offset + 1;

  const renderConfiguration = (configuration: ISmsServerConfiguration) => {
    const isSelected = selectedId === configuration.id;
    return (
      <div
        key={configuration.id}
        onClick={() => setSelectedId(configuration.id)}
        className={cn(
          'cursor-pointer rounded-lg border p-4 transition-all',
          isSelected
            ? 'border-primary bg-primary/10 ring-2 ring-primary/20'
            : 'border-card-border hover:border-primary/40',
        )}
      >
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="mb-1 flex flex-wrap items-center gap-2">
              <h4 className="font-medium text-foreground">
                {configuration.name}
              </h4>
              {configuration.default && (
                <Badge variant="outline" className="text-xs">
                  Default
                </Badge>
              )}
            </div>
            <p className="text-sm text-muted-foreground">
              {getSmsProviderLabel(configuration.provider)} · Sender ID:{' '}
              {configuration.senderId}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <SmsServerConfigurationStatusBadge status={configuration.status} />
            {isSelected && <CheckIcon className="size-5 text-primary" />}
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="mx-auto max-w-[80%]">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-foreground">
          Select SMS Server Configuration
        </h2>
        <p className="mt-2 text-muted-foreground">
          Choose the SMS gateway configuration to send this campaign.
        </p>
      </div>

      <div className="relative mb-4 flex w-full items-center gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search by name or sender ID..."
            value={searchValue}
            onChange={e => {
              setSearchValue(e.currentTarget.value);
              setOffset(0);
            }}
            className="w-full pl-9"
          />
        </div>
        <Button variant="destructive" onClick={handleReset}>
          <RotateCcw className="mr-2 size-4" />
          Reset
        </Button>
      </div>

      {selectedId && (
        <div className="mb-4 flex items-center gap-2 rounded-lg border border-primary/30 bg-primary/10 px-4 py-2.5">
          <div className="flex size-5 shrink-0 items-center justify-center rounded-full bg-primary">
            <CheckIcon className="size-3 text-white" />
          </div>
          Configuration:{' '}
          <span className="text-sm font-medium text-primary">
            {selectedName || 'Configuration selected'}
          </span>
        </div>
      )}

      <div className="mb-6 max-h-96 space-y-3 overflow-y-auto">
        {loading ? (
          Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="animate-pulse rounded-lg border p-4">
              <div className="mb-2 h-5 w-1/3 rounded bg-card-border" />
              <div className="h-4 w-1/2 rounded bg-card-border" />
            </div>
          ))
        ) : activeItems.length === 0 ? (
          <div className="py-12 text-center text-muted-foreground">
            <p className="mb-2">No active SMS server configurations found.</p>
            <Link
              to={routes.smsServerConfigurations.path}
              className="text-sm text-primary hover:underline"
            >
              Create an SMS server configuration
            </Link>
          </div>
        ) : (
          activeItems.map(renderConfiguration)
        )}
      </div>

      {configurations.total > PAGE_SIZE && (
        <div className="mb-6 flex items-center justify-between">
          <span className="text-sm text-muted-foreground">
            Showing {activeItems.length} of {configurations.total}{' '}
            configurations
          </span>
          <Pagination
            total={configurations.total}
            perPage={PAGE_SIZE}
            currentPage={currentPage}
            onPageChange={page => setOffset(page - 1)}
          />
        </div>
      )}

      <div className="flex justify-between border-t border-card-border pt-6">
        <Button variant="outline" onClick={onBack} disabled={isLoading}>
          <ArrowLeftIcon className="size-4" />
          Back
        </Button>
        <Button
          onClick={handleSubmit}
          disabled={!selectedId || isLoading}
        >
          {isLoading ? 'Saving...' : 'Save & Continue'}
          <ArrowRightIcon className="size-4" />
        </Button>
      </div>
    </div>
  );
};
