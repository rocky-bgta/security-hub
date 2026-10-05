import { Search, X } from 'lucide-react';
import type { BreachType, Severity, ThreatStatus } from 'models/BreachMonitor';

interface FilterState {
  search: string;
  severity: Severity | 'all';
  type: BreachType | 'all';
  status: ThreatStatus | 'all';
}

interface Props {
  filters: FilterState;
  onChange: (filters: FilterState) => void;
  showTypeFilter?: boolean;
}

const BreachFilters = ({
  filters,
  onChange,
  showTypeFilter = false,
}: Props) => {
  const hasActive =
    filters.search ||
    filters.severity !== 'all' ||
    filters.status !== 'all' ||
    filters.type !== 'all';

  return (
    <div className="mb-4 flex flex-wrap items-center gap-3">
      <div className="relative min-w-[200px] max-w-sm flex-1">
        <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
        <input
          type="text"
          placeholder="Search breaches..."
          value={filters.search}
          onChange={e => onChange({ ...filters, search: e.target.value })}
          className="w-full rounded-md border border-card-border bg-card py-2 pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
        />
      </div>

      <select
        value={filters.severity}
        onChange={e =>
          onChange({ ...filters, severity: e.target.value as Severity | 'all' })
        }
        className="rounded-md border border-card-border bg-card px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
      >
        <option value="all">All Severities</option>
        <option value="critical">Critical</option>
        <option value="high">High</option>
        <option value="medium">Medium</option>
        <option value="low">Low</option>
      </select>

      <select
        value={filters.status}
        onChange={e =>
          onChange({
            ...filters,
            status: e.target.value as ThreatStatus | 'all',
          })
        }
        className="rounded-md border border-card-border bg-card px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
      >
        <option value="all">All Statuses</option>
        <option value="open">Open</option>
        <option value="in_mitigation">In Mitigation</option>
        <option value="completed">Completed</option>
      </select>

      {showTypeFilter && (
        <select
          value={filters.type}
          onChange={e =>
            onChange({ ...filters, type: e.target.value as BreachType | 'all' })
          }
          className="rounded-md border border-card-border bg-card px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
        >
          <option value="all">All Types</option>
          <option value="email">Email</option>
          <option value="ip">IP</option>
        </select>
      )}

      {hasActive && (
        <button
          onClick={() =>
            onChange({
              search: '',
              severity: 'all',
              type: 'all',
              status: 'all',
            })
          }
          className="inline-flex items-center gap-1 px-3 py-2 text-xs font-medium text-muted-foreground transition-colors hover:text-foreground"
        >
          <X className="size-3.5" />
          Clear
        </button>
      )}
    </div>
  );
};

export default BreachFilters;
export type { FilterState };
