import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import { Popover, PopoverContent, PopoverTrigger } from 'common/Popover';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';

import { Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import { Filter } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';

import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IGetListParams, IResponse } from 'models/Global';
import {
  ISupportTicketRecentTickets,
  ISupportTicketReportSummary,
  ISupportTicketStatusDistribution,
} from 'models/SupportTicket';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  formateDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

interface ISupportTicketRecentQueryParams extends IGetListParams {
  fromDate?: string;
  toDate?: string;
}

const CHART_COLORS = [
  'hsl(173, 58%, 39%)',
  'hsl(45, 93%, 47%)',
  'hsl(0, 84%, 60%)',
  'hsl(180, 62%, 30%)',
  'hsl(215, 20%, 65%)',
];

const RADIAN = Math.PI / 180;

interface IPieLabelProps {
  cx?: number;
  cy?: number;
  midAngle?: number;
  outerRadius?: number;
  name?: string;
  value?: number;
  fill?: string;
  payload?: { fill?: string };
}

const renderStatusDistributionLabel = ({
  cx = 0,
  cy = 0,
  midAngle = 0,
  outerRadius = 0,
  name = '',
  value = 0,
  fill,
  payload,
}: IPieLabelProps) => {
  const sliceColor = fill ?? payload?.fill ?? 'hsl(215, 20%, 65%)';
  const labelRadius = outerRadius + 18;
  const x = cx + labelRadius * Math.cos(-midAngle * RADIAN);
  const y = cy + labelRadius * Math.sin(-midAngle * RADIAN);

  return (
    <text
      x={x}
      y={y}
      fill={sliceColor}
      textAnchor={x > cx ? 'start' : 'end'}
      dominantBaseline="central"
      fontSize={16}
      fontWeight={500}
    >
      {`${name}: ${value}`}
    </text>
  );
};

interface IPieLabelLineProps {
  points?: Array<{ x: number; y: number }>;
  payload?: { fill?: string };
}

const renderStatusDistributionLabelLine = ({
  points,
  payload,
}: IPieLabelLineProps) => {
  if (!points?.length) {
    return <polyline points="" stroke="none" fill="none" />;
  }

  return (
    <polyline
      points={points.map(point => `${point.x},${point.y}`).join(' ')}
      stroke={payload?.fill ?? 'hsl(215, 20%, 65%)'}
      strokeWidth={1}
      fill="none"
    />
  );
};

const formatStatusLabel = (status: string) => {
  const label = status.toLowerCase().replace(/_/g, ' ');
  return label.charAt(0).toUpperCase() + label.slice(1);
};

interface IStatusDistributionTooltipProps {
  active?: boolean;
  payload?: Array<{
    name?: string;
    value?: number;
    payload?: { fill?: string };
  }>;
}

const StatusDistributionTooltip = ({
  active,
  payload,
}: IStatusDistributionTooltipProps) => {
  if (!active || !payload?.length) return null;

  const { name, value, payload: data } = payload[0];

  return (
    <div className="rounded-lg border border-card-border bg-card-background px-3 py-2 shadow-lg">
      <div className="flex items-center gap-2">
        <span
          className="size-2.5 shrink-0 rounded-full"
          style={{ backgroundColor: data?.fill }}
        />
        <span className="text-base font-medium text-card-foreground">
          {name}
        </span>
      </div>
      <p className="ml-4 mt-1 text-base text-muted-foreground">
        Count: {value}
      </p>
    </div>
  );
};

const StatusBadge = ({ status }: { status: string }) => {
  const variants: Record<string, string> = {
    open: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
    in_progress: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    closed: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
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

const PriorityBadge = ({ priority }: { priority: string }) => {
  const variants: Record<string, string> = {
    high: 'bg-red-500/20 text-red-400 border-red-500/30',
    medium: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    low: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
  };
  const cls =
    variants[priority.toLowerCase()] ||
    'bg-muted text-muted-foreground border-border';
  return (
    <Badge variant="outline" className={`${cls} text-xs capitalize`}>
      {priority.toLowerCase()}
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

const OpenCloseTicketReport = () => {
  const apiClient = useAPI();
  const [summary, setSummary] = useState<ISupportTicketReportSummary>({
    totalTickets: 0,
    openTickets: 0,
    closedTickets: 0,
    highPriorityTickets: 0,
  });
  const [statusDistribution, setStatusDistribution] = useState<
    ISupportTicketStatusDistribution[]
  >([]);
  const [recentTickets, setRecentTickets] =
    useState<ISupportTicketRecentTickets>({
      items: [],
      total: 0,
      pageSize: 10,
      offset: 0,
    });
  const [paginationKey, setPaginationKey] = useState<number>(0);
  const [isFilterPopoverOpen, setIsFilterPopoverOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [queryParams, setQueryParams] =
    useState<ISupportTicketRecentQueryParams>({
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
    const fetchSummaryAndDistribution = async () => {
      try {
        const summaryResponse: IResponse<ISupportTicketReportSummary> =
          await apiClient.get(API_END_POINTS.GET_SUPPORT_TICKET_REPORT_SUMMARY);

        if (isSuccessResponse(summaryResponse.statusCode)) {
          setSummary(summaryResponse.data);
        }

        const distributionResponse: IResponse<
          ISupportTicketStatusDistribution[]
        > = await apiClient.get(
          API_END_POINTS.GET_SUPPORT_TICKET_STATUS_DISTRIBUTION,
        );

        if (isSuccessResponse(distributionResponse.statusCode)) {
          setStatusDistribution(distributionResponse.data);
        }
      } catch (error) {
        console.error('Error fetching support ticket report summary:', error);
      }
    };

    fetchSummaryAndDistribution();
  }, [apiClient]);

  useEffect(() => {
    const fetchRecentTickets = async () => {
      try {
        const response: IResponse<ISupportTicketRecentTickets> =
          await apiClient.get(
            API_END_POINTS.GET_SUPPORT_TICKET_RECENT_TICKETS + queryString,
          );

        if (isSuccessResponse(response.statusCode)) {
          setRecentTickets(response.data);
        }
      } catch (error) {
        console.error('Error fetching recent support tickets:', error);
      }
    };

    fetchRecentTickets();
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

  const statusBreakdown = useMemo(
    () =>
      statusDistribution
        .filter(item => item.count > 0)
        .map((item, index) => ({
          ...item,
          status: formatStatusLabel(item.status),
          fill: CHART_COLORS[index % CHART_COLORS.length],
        })),
    [statusDistribution],
  );

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard label="Total Tickets" value={summary.totalTickets} />
        <StatCard
          label="Open"
          value={summary.openTickets}
          color="text-blue-400"
        />
        <StatCard
          label="Closed"
          value={summary.closedTickets}
          color="text-emerald-400"
        />
        <StatCard
          label="High Priority"
          value={summary.highPriorityTickets}
          color="text-destructive"
        />
      </div>
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Status Distribution</CardTitle>
        </CardHeader>
        <CardContent>
          {!statusBreakdown.length ? (
            <p className="py-6 text-center text-sm text-muted-foreground">
              No data available
            </p>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={statusBreakdown}
                  dataKey="count"
                  nameKey="status"
                  cx="50%"
                  cy="50%"
                  outerRadius={120}
                  stroke="none"
                  label={renderStatusDistributionLabel}
                  labelLine={renderStatusDistributionLabelLine}
                />
                <Tooltip
                  content={<StatusDistributionTooltip />}
                  wrapperStyle={{ zIndex: 50, outline: 'none' }}
                />
              </PieChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Recent Tickets</CardTitle>
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
              placeholder="Search by ticket ID or subject"
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
                <TableHead>ID</TableHead>
                <TableHead>Subject</TableHead>
                <TableHead>User</TableHead>
                <TableHead>Priority</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created</TableHead>
                <TableHead>Category</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {!recentTickets.items.length ? (
                <TableRow>
                  <TableCell
                    colSpan={7}
                    className="py-8 text-center text-muted-foreground"
                  >
                    No data available
                  </TableCell>
                </TableRow>
              ) : (
                recentTickets.items.map(ticket => (
                  <TableRow key={ticket.ticketId}>
                    <TableCell className="font-medium">
                      {ticket.ticketId}
                    </TableCell>
                    <TableCell>{ticket.subject}</TableCell>
                    <TableCell>{ticket.user}</TableCell>
                    <TableCell>
                      <PriorityBadge priority={ticket.priority} />
                    </TableCell>
                    <TableCell>
                      <StatusBadge status={ticket.status} />
                    </TableCell>
                    <TableCell>
                      {formateDateAndTime(ticket.createdDate)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {ticket.category}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>

          <div className="flex justify-end pt-6">
            <Pagination
              key={paginationKey}
              total={recentTickets.total}
              perPage={recentTickets.pageSize}
              onPageChange={handlePageChange}
            />
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default OpenCloseTicketReport;
