import { Button } from 'common/Button';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import RecipientBreachTable from 'features/breach/RecipientBreachTable';
import { IRecipientBreachListParams, useBreaches } from 'hooks/UseBreaches';
import { IRecipientBreach, RecipientBreachStatus } from 'models/Breach';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { routes } from 'routes/Routes';

/**
 * Recipient Breaches Page
 * Dedicated page for viewing and managing recipient breaches
 * Based on Task-08 requirements (AC-06, AC-07, AC-08, AC-09, AC-10, AC-14)
 */
const RecipientBreaches: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const breachRecordIdParam = searchParams.get('breachId');

  const {
    loading,
    actionLoading,
    fetchRecipientBreaches,
    notifyRecipient,
    resetRecipientPassword,
    resolveRecipientBreach,
  } = useBreaches();

  // Data state
  const [recipients, setRecipients] = useState<IRecipientBreach[]>([]);
  const [totalRecipients, setTotalRecipients] = useState(0);

  // Filter state
  const [filters, setFilters] = useState<IRecipientBreachListParams>({
    offset: 0,
    pageSize: 20,
    breachRecordId: breachRecordIdParam || undefined,
  });
  const [searchKeyword, setSearchKeyword] = useState('');
  const [filterStatus, setFilterStatus] = useState<RecipientBreachStatus | ''>(
    '',
  );

  // Stats
  const [stats, setStats] = useState({
    pending: 0,
    notified: 0,
    resolved: 0,
  });

  // Load recipients
  const loadRecipients = useCallback(async () => {
    const params: IRecipientBreachListParams = {
      ...filters,
      keyword: searchKeyword || undefined,
      status: filterStatus || undefined,
    };

    const result = await fetchRecipientBreaches(params);
    if (result) {
      setRecipients(result.items || []);
      setTotalRecipients(result.total || 0);

      // Calculate stats from current results
      const items = result.items || [];
      setStats({
        pending: items.filter(r => r.status === RecipientBreachStatus.PENDING)
          .length,
        notified: items.filter(r => r.status === RecipientBreachStatus.NOTIFIED)
          .length,
        resolved: items.filter(r => r.status === RecipientBreachStatus.RESOLVED)
          .length,
      });
    }
  }, [filters, searchKeyword, filterStatus, fetchRecipientBreaches]);

  useEffect(() => {
    setTimeout(() => {
      loadRecipients();
    }, 0);
  }, [loadRecipients]);

  // Update breach filter from URL param
  useEffect(() => {
    if (breachRecordIdParam) {
      setTimeout(() => {
        setFilters(prev => ({ ...prev, breachRecordId: breachRecordIdParam }));
      }, 0);
    }
  }, [breachRecordIdParam]);

  // Action handlers
  const handleNotifyRecipient = async (id: string, notes?: string) => {
    const result = await notifyRecipient(id, { notes, sendEmail: true });
    if (result) {
      toast.success('User notified successfully');
      loadRecipients();
    } else {
      toast.error('Failed to notify user');
    }
  };

  const handleResetPassword = async (id: string, notes?: string) => {
    const result = await resetRecipientPassword(id, { notes });
    if (result) {
      toast.success('Password reset triggered');
      loadRecipients();
    } else {
      toast.error('Failed to trigger password reset');
    }
  };

  const handleResolveRecipient = async (id: string, notes?: string) => {
    const result = await resolveRecipientBreach(id, { notes });
    if (result) {
      toast.success('Breach resolved for user');
      loadRecipients();
    } else {
      toast.error('Failed to resolve breach');
    }
  };

  // Pagination
  const handlePageChange = (offset: number) => {
    setFilters(prev => ({ ...prev, offset }));
  };

  // Filter handlers
  const handleApplyFilters = () => {
    setFilters(prev => ({ ...prev, offset: 0 }));
    loadRecipients();
  };

  const handleClearFilters = () => {
    setSearchKeyword('');
    setFilterStatus('');
    setFilters({
      offset: 0,
      pageSize: 20,
      breachRecordId: undefined,
    });
  };

  const handleClearBreachFilter = () => {
    setFilters(prev => ({ ...prev, breachRecordId: undefined, offset: 0 }));
    // Update URL
    navigate(routes.recipientBreaches.path, { replace: true });
  };

  // Page size
  const currentPage =
    Math.floor((filters.offset || 0) / (filters.pageSize || 20)) + 1;
  const totalPages = Math.ceil(totalRecipients / (filters.pageSize || 20));

  return (
    <div className="min-h-screen p-6">
      {/* Header */}
      <div className="mb-6">
        <div className="mb-2 flex items-center gap-2 text-sm text-muted-foreground">
          <button
            onClick={() => navigate(routes.breachDashboard.path)}
            className="hover:text-primary"
          >
            Breach Dashboard
          </button>
          <span>/</span>
          <span className="text-foreground">Recipient Breaches</span>
        </div>
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-foreground">
              Recipient Breaches
            </h1>
            <p className="text-muted-foreground">
              View and manage users affected by data breaches
            </p>
          </div>
          <button
            onClick={() => navigate(routes.breachManagement.path)}
            className="hover: rounded-lg border border-card-border bg-card-background px-4 py-2 text-sm font-medium text-card-foreground"
          >
            View Breaches
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-3">
        <div
          onClick={() => setFilterStatus(RecipientBreachStatus.PENDING)}
          className={`cursor-pointer rounded-lg border bg-card-background p-4 transition-colors ${
            filterStatus === RecipientBreachStatus.PENDING
              ? 'border-primary ring-1 ring-primary'
              : 'border-card-border hover:border-card-border'
          }`}
        >
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-primary/10 p-2">
              <svg
                className="size-5 text-primary"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <div>
              <p className="text-2xl font-bold text-foreground">
                {stats.pending}
              </p>
              <p className="text-sm text-muted-foreground">Pending</p>
            </div>
          </div>
        </div>

        <div
          onClick={() => setFilterStatus(RecipientBreachStatus.NOTIFIED)}
          className={`cursor-pointer rounded-lg border bg-card-background p-4 transition-colors ${
            filterStatus === RecipientBreachStatus.NOTIFIED
              ? 'border-primary ring-1 ring-primary'
              : 'border-card-border hover:border-card-border'
          }`}
        >
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-primary/10 p-2">
              <svg
                className="size-5 text-primary"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
                />
              </svg>
            </div>
            <div>
              <p className="text-2xl font-bold text-foreground">
                {stats.notified}
              </p>
              <p className="text-sm text-muted-foreground">Notified</p>
            </div>
          </div>
        </div>

        <div
          onClick={() => setFilterStatus(RecipientBreachStatus.RESOLVED)}
          className={`cursor-pointer rounded-lg border bg-card-background p-4 transition-colors ${
            filterStatus === RecipientBreachStatus.RESOLVED
              ? 'border-primary ring-1 ring-primary'
              : 'border-card-border hover:border-card-border'
          }`}
        >
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-primary/10 p-2">
              <svg
                className="size-5 text-primary"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
            <div>
              <p className="text-2xl font-bold text-foreground">
                {stats.resolved}
              </p>
              <p className="text-sm text-muted-foreground">Resolved</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="rounded-xl border border-card-border bg-card-background shadow-sm">
        {/* Filters */}
        <div className="border-b border-card-border px-6 py-4">
          <div className="flex items-center gap-4">
            <div className="min-w-[280px] flex-1">
              <div className="relative">
                <svg
                  className="absolute left-3 top-1/2 size-5 -translate-y-1/2 text-muted-foreground"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
                <Input
                  type="text"
                  value={searchKeyword}
                  onChange={e => setSearchKeyword(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && handleApplyFilters()}
                  placeholder="Search by name or email..."
                  className="pl-10"
                />
              </div>
            </div>

            <Select
              value={filterStatus}
              onValueChange={value => {
                setFilterStatus(value as RecipientBreachStatus | '');
                setFilters(prev => ({ ...prev, offset: 0 }));
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select a status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={RecipientBreachStatus.PENDING}>
                  Pending
                </SelectItem>
                <SelectItem value={RecipientBreachStatus.NOTIFIED}>
                  Notified
                </SelectItem>
                <SelectItem value={RecipientBreachStatus.RESOLVED}>
                  Resolved
                </SelectItem>
              </SelectContent>
            </Select>

            <Button variant="default" onClick={handleApplyFilters}>
              Apply Filters
            </Button>

            {(searchKeyword || filterStatus || filters.breachRecordId) && (
              <button
                onClick={handleClearFilters}
                className="rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200"
              >
                Clear Filters
              </button>
            )}
          </div>

          {/* Active filter badges */}
          {filters.breachRecordId && (
            <div className="mt-3 flex items-center gap-2">
              <span className="text-sm text-gray-500">Filtered by breach:</span>
              <span className="inline-flex items-center gap-1 rounded bg-primary/10 px-2 py-1 text-sm text-primary">
                Breach ID: {filters.breachRecordId.slice(0, 8)}...
                <button
                  onClick={handleClearBreachFilter}
                  className="ml-1 hover:text-primary"
                >
                  <svg
                    className="size-4"
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
              </span>
            </div>
          )}
        </div>

        {/* Table */}
        <div className="px-6 py-4">
          <RecipientBreachTable
            recipients={recipients}
            loading={loading}
            onNotify={handleNotifyRecipient}
            onResetPassword={handleResetPassword}
            onResolve={handleResolveRecipient}
            actionLoading={actionLoading}
          />
        </div>

        {/* Pagination */}
        {totalRecipients > (filters.pageSize || 20) && (
          <div className="flex items-center justify-between border-t border-card-border px-6 py-4">
            <p className="text-sm text-muted-foreground">
              Showing {(filters.offset || 0) + 1} to{' '}
              {Math.min(
                (filters.offset || 0) + (filters.pageSize || 20),
                totalRecipients,
              )}{' '}
              of {totalRecipients} recipients
            </p>
            <div className="flex items-center gap-2">
              <button
                onClick={() =>
                  handlePageChange(
                    (filters.offset || 0) - (filters.pageSize || 20),
                  )
                }
                disabled={filters.offset === 0}
                className="hover: rounded-lg border border-card-border bg-card-background px-4 py-2 text-sm font-medium text-card-foreground disabled:cursor-not-allowed disabled:opacity-50"
              >
                Previous
              </button>

              <div className="flex items-center gap-1">
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                  let pageNum: number;
                  if (totalPages <= 5) {
                    pageNum = i + 1;
                  } else if (currentPage <= 3) {
                    pageNum = i + 1;
                  } else if (currentPage >= totalPages - 2) {
                    pageNum = totalPages - 4 + i;
                  } else {
                    pageNum = currentPage - 2 + i;
                  }

                  return (
                    <button
                      key={pageNum}
                      onClick={() =>
                        handlePageChange(
                          (pageNum - 1) * (filters.pageSize || 20),
                        )
                      }
                      className={`rounded px-3 py-1 text-sm font-medium ${
                        currentPage === pageNum
                          ? 'bg-primary text-primary-foreground'
                          : 'text-card-foreground hover:bg-card-border'
                      }`}
                    >
                      {pageNum}
                    </button>
                  );
                })}
              </div>

              <button
                onClick={() =>
                  handlePageChange(
                    (filters.offset || 0) + (filters.pageSize || 20),
                  )
                }
                disabled={
                  (filters.offset || 0) + (filters.pageSize || 20) >=
                  totalRecipients
                }
                className="hover: rounded-lg border border-card-border bg-card-background px-4 py-2 text-sm font-medium text-card-foreground disabled:cursor-not-allowed disabled:opacity-50"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Bulk Actions Info */}
      <div className="mt-6 rounded-lg border border-primary/20 bg-primary/10 p-4">
        <div className="flex items-start gap-3">
          <svg
            className="size-5 text-primary"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <div>
            <h4 className="text-sm font-medium text-foreground">
              Available Actions
            </h4>
            <ul className="mt-1 space-y-1 text-sm text-muted-foreground">
              <li>
                <strong>Notify:</strong> Send an email notification to the user
                about the breach
              </li>
              <li>
                <strong>Reset Password:</strong> Trigger a password reset for
                the affected user
              </li>
              <li>
                <strong>Resolve:</strong> Mark the breach as resolved for this
                user
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RecipientBreaches;
