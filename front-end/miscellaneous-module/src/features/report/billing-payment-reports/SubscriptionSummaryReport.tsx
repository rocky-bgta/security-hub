import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import { Popover, PopoverContent, PopoverTrigger } from 'common/Popover';
import { Progress } from 'common/Progress';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';

import { Filter } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';

import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IGetListParams, IResponse } from 'models/Global';
import {
  ISubscriptionSummaryDetailItem,
  ISubscriptionSummaryReportData,
} from 'models/SubscriptionSummaryReport';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  formateDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

interface ISubscriptionSummaryQueryParams extends IGetListParams {
  fromDate?: string;
  toDate?: string;
}

const formatStatusLabel = (status: string) => {
  const label = status.toLowerCase().replace(/_/g, ' ');
  return label.charAt(0).toUpperCase() + label.slice(1);
};

const StatusBadge = ({ status }: { status: string }) => {
  const variants: Record<string, string> = {
    active: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    expired: 'bg-red-500/20 text-red-400 border-red-500/30',
    expiring: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    expiring_soon: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    pending: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    inactive: 'bg-muted text-muted-foreground border-border',
    suspended: 'bg-red-500/20 text-red-400 border-red-500/30',
  };
  const cls =
    variants[status.toLowerCase()] ||
    'bg-muted text-muted-foreground border-border';
  return (
    <Badge variant="outline" className={`${cls} text-xs`}>
      {formatStatusLabel(status)}
    </Badge>
  );
};

const StatCard = ({
  label,
  value,
  color,
}: {
  label: string;
  value: string | number;
  color?: string;
}) => (
  <Card>
    <CardContent className="p-4">
      <div className={`text-2xl font-bold ${color || ''}`}>{value}</div>
      <div className="mt-1 text-xs text-muted-foreground">{label}</div>
    </CardContent>
  </Card>
);

const SubscriptionSummaryReport = () => {
  const apiClient = useAPI();
  const [summary, setSummary] = useState({
    totalSubscriptions: 0,
    active: 0,
    expired: 0,
    expiringSoon: 0,
  });
  const [subscriptions, setSubscriptions] = useState<{
    items: ISubscriptionSummaryDetailItem[];
    total: number;
    pageSize: number;
    offset: number;
  }>({
    items: [],
    total: 0,
    pageSize: 10,
    offset: 0,
  });
  const [paginationKey, setPaginationKey] = useState<number>(0);
  const [isFilterPopoverOpen, setIsFilterPopoverOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [queryParams, setQueryParams] =
    useState<ISubscriptionSummaryQueryParams>({
      ...InitGetListParams,
      search: '',
      fromDate: '',
      toDate: '',
      offset: 0,
      pageSize: 10,
    });
  const debouncedSearch = useDebounce(searchTerm, 500);
  const [draftFilters, setDraftFilters] = useState({
    fromDate: '',
    toDate: '',
  });

  const effectiveQueryParams = useMemo(
    () => ({ ...queryParams, search: debouncedSearch }),
    [debouncedSearch, queryParams],
  );

  const queryString = useMemo(
    () => objectToQueryString(effectiveQueryParams),
    [effectiveQueryParams],
  );

  useEffect(() => {
    const fetchSubscriptionSummary = async () => {
      try {
        const response: IResponse<ISubscriptionSummaryReportData> =
          await apiClient.get(
            API_END_POINTS.GET_SUBSCRIPTION_SUMMARY_REPORT + queryString,
          );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'API error');
        }

        const data = response.data;
        setSummary({
          totalSubscriptions: data.totalSubscriptions,
          active: data.active,
          expired: data.expired,
          expiringSoon: data.expiringSoon,
        });
        setSubscriptions(data.subscriptions);
      } catch (error) {
        console.error('Error fetching subscription summary report:', error);
      }
    };

    fetchSubscriptionSummary();
  }, [apiClient, queryString]);

  const resetToFirstPage = () => {
    setPaginationKey(prev => prev + 1);
  };

  const handleResetQueries = () => {
    setSearchTerm('');
    setQueryParams({
      ...InitGetListParams,
      search: '',
      fromDate: '',
      toDate: '',
      offset: 0,
      pageSize: 10,
    });
    setDraftFilters({
      fromDate: '',
      toDate: '',
    });
    resetToFirstPage();
  };

  const handleFilterPopoverOpenChange = (isOpen: boolean) => {
    if (isOpen) {
      setDraftFilters({
        fromDate: queryParams.fromDate || '',
        toDate: queryParams.toDate || '',
      });
    }
    setIsFilterPopoverOpen(isOpen);
  };

  const handleApplyFilters = () => {
    setQueryParams(prevState => ({
      ...prevState,
      fromDate: draftFilters.fromDate,
      toDate: draftFilters.toDate,
      offset: 0,
    }));
    resetToFirstPage();
    setIsFilterPopoverOpen(false);
  };

  const handlePageChange = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard
          label="Total Subscriptions"
          value={summary.totalSubscriptions}
        />
        <StatCard
          label="Active"
          value={summary.active}
          color="text-emerald-400"
        />
        <StatCard
          label="Expired"
          value={summary.expired}
          color="text-destructive"
        />
        <StatCard
          label="Expiring Soon"
          value={summary.expiringSoon}
          color="text-yellow-400"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Subscription Details</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mb-4 grid gap-3 md:grid-cols-9">
            <Input
              value={searchTerm}
              onChange={event => {
                setSearchTerm(event.target.value);
                setQueryParams(prevState => ({
                  ...prevState,
                  offset: 0,
                }));
                resetToFirstPage();
              }}
              placeholder="Search by client or plan"
              className="md:col-span-7"
            />

            <div className="flex items-center gap-2 md:col-span-2">
              <Popover
                open={isFilterPopoverOpen}
                onOpenChange={handleFilterPopoverOpenChange}
              >
                <PopoverTrigger asChild>
                  <Button variant="outline" className="w-full">
                    <Filter className="mr-2 size-4" />
                    Filter
                  </Button>
                </PopoverTrigger>
                <PopoverContent align="end" className="w-full space-y-4">
                  <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                    <div className="space-y-2">
                      <p className="text-sm font-medium">From Date</p>
                      <Input
                        type="date"
                        value={draftFilters.fromDate}
                        onChange={event =>
                          setDraftFilters(prevState => ({
                            ...prevState,
                            fromDate: event.target.value,
                          }))
                        }
                      />
                    </div>

                    <div className="space-y-2">
                      <p className="text-sm font-medium">To Date</p>
                      <Input
                        type="date"
                        value={draftFilters.toDate}
                        onChange={event =>
                          setDraftFilters(prevState => ({
                            ...prevState,
                            toDate: event.target.value,
                          }))
                        }
                      />
                    </div>
                  </div>

                  <div className="flex justify-end">
                    <Button type="button" onClick={handleApplyFilters}>
                      Apply
                    </Button>
                  </div>
                </PopoverContent>
              </Popover>

              <Button
                variant="destructive"
                className="w-full"
                onClick={handleResetQueries}
              >
                Reset
              </Button>
            </div>
          </div>

          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Client</TableHead>
                <TableHead>Plan</TableHead>
                <TableHead>Start</TableHead>
                <TableHead>End</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Usage</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {subscriptions.items.map((subscription, index) => (
                <TableRow key={`${subscription.client}-${index}`}>
                  <TableCell className="font-medium">
                    {subscription.client}
                  </TableCell>
                  <TableCell>{subscription.plan}</TableCell>
                  <TableCell>
                    {formateDateAndTime(subscription.startDate)}
                  </TableCell>
                  <TableCell>
                    {formateDateAndTime(subscription.endDate)}
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={subscription.status} />
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Progress
                        value={subscription.usage}
                        className="h-2 w-16"
                      />
                      <span className="text-xs">{subscription.usage}%</span>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          <div className="flex justify-end pt-6">
            <Pagination
              key={paginationKey}
              total={subscriptions.total}
              perPage={subscriptions.pageSize}
              onPageChange={handlePageChange}
            />
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default SubscriptionSummaryReport;
