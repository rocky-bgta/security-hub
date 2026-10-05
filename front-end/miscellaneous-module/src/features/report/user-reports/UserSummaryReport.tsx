import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import { Popover, PopoverContent, PopoverTrigger } from 'common/Popover';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';

import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import {
  Building2,
  Filter,
  Info,
  Lock,
  LucideIcon,
  User,
  UserCheck,
  Users,
} from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';

import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IGetListParams, IResponse } from 'models/Global';
import {
  IUserSummaryReportData,
  TPlatformGrowthTrendRange,
  TRiskGroup,
} from 'models/UserSummaryReport';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  formateDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

interface IUserSummaryQueryParams extends IGetListParams {
  fromDate?: string;
  toDate?: string;
  userType?: string;
  trendMonths?: number;
  platformGrowthTrendRange?: TPlatformGrowthTrendRange;
}

const PLATFORM_GROWTH_RANGE_OPTIONS: Array<{
  value: TPlatformGrowthTrendRange;
  label: string;
}> = [
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'QUARTERLY', label: 'Quarterly' },
  { value: 'HALF_YEARLY', label: 'Half Yearly' },
  { value: 'YEARLY', label: 'Yearly' },
];

const PLATFORM_GROWTH_SERIES = [
  { key: 'totalMsp', name: 'MSPs', color: '#3b82f6', yAxisId: 'left' },
  {
    key: 'totalClientAdmin',
    name: 'Clients',
    color: '#22c55e',
    yAxisId: 'left',
  },
  {
    key: 'totalLicenseUser',
    name: 'Licensed Users',
    color: '#a855f7',
    yAxisId: 'right',
  },
  {
    key: 'totalActiveUser',
    name: 'Active Licensed Users',
    color: '#14b8a6',
    yAxisId: 'left',
  },
] as const;

const StatusBadge = ({
  status,
  riskGroup,
}: {
  status?: string;
  riskGroup?: TRiskGroup;
}) => {
  const variants: Record<string, string> = {
    active: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    completed: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    paid: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    valid: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    success: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    closed: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    resolved: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    suspended: 'bg-red-500/20 text-red-400 border-red-500/30',
    failed: 'bg-red-500/20 text-red-400 border-red-500/30',
    overdue: 'bg-red-500/20 text-red-400 border-red-500/30',
    expired: 'bg-red-500/20 text-red-400 border-red-500/30',
    critical: 'bg-red-500/20 text-red-400 border-red-500/30',
    alert: 'bg-red-500/20 text-red-400 border-red-500/30',
    disputed: 'bg-red-500/20 text-red-400 border-red-500/30',
    inactive: 'bg-muted text-muted-foreground border-border',
    pending: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    expiring: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    expiring_soon: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    not_started: 'bg-muted text-muted-foreground border-border',
    flagged: 'bg-orange-500/20 text-orange-400 border-orange-500/30',
    partial: 'bg-orange-500/20 text-orange-400 border-orange-500/30',
    in_progress: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
    open: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
    upcoming: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
    unpaid: 'bg-orange-500/20 text-orange-400 border-orange-500/30',
  };
  const riskVariants: Record<TRiskGroup, string> = {
    LOW_RISK: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    MEDIUM_RISK: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    HIGH_RISK: 'bg-red-500/20 text-red-400 border-red-500/30',
    CRITICAL_RISK: 'bg-red-500/20 text-red-400 border-red-500/30',
  };
  const cls =
    variants[status || ''] ||
    riskVariants[riskGroup || 'LOW_RISK'] ||
    'bg-muted text-muted-foreground border-border';
  return (
    <Badge variant="outline" className={`${cls} text-xs capitalize`}>
      {status}
    </Badge>
  );
};

const StatCard = ({
  label,
  value,
  icon: Icon,
  iconClassName,
}: {
  label: string;
  value: string | number;
  icon: LucideIcon;
  iconClassName: string;
}) => (
  <Card>
    <CardContent className="flex items-center gap-3 p-4">
      <div
        className={`flex size-10 shrink-0 items-center justify-center rounded-lg bg-muted/50 ${iconClassName}`}
      >
        <Icon className="size-5" />
      </div>
      <div className="min-w-0">
        <div className="text-xs text-muted-foreground">{label}</div>
        <div className="text-2xl font-bold text-foreground">
          {typeof value === 'number' ? value.toLocaleString() : value}
        </div>
      </div>
    </CardContent>
  </Card>
);

const UserSummaryReport = () => {
  const apiClient = useAPI();
  const [reportData, setReportData] = useState<IUserSummaryReportData>({
    totals: {
      totalUsers: 0,
      activeUsers: 0,
      suspendedUsers: 0,
      newSignupsLast30Days: 0,
    },
    growthTrend: [],
    platformGrowthTrend: [],
    userCardInfo: {
      totalMsp: 0,
      totalClientAdmin: 0,
      totalLicenseUser: 0,
      totalActiveUser: 0,
      totalSuspendedUser: 0,
    },
    details: {
      items: [],
      total: 0,
      pageSize: 10,
      offset: 0,
    },
  });
  const [paginationKey, setPaginationKey] = useState<number>(0);
  const [isFilterPopoverOpen, setIsFilterPopoverOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IUserSummaryQueryParams>({
    ...InitGetListParams,
    search: '',
    status: '',
    userType: '',
    fromDate: '',
    toDate: '',
    offset: 0,
    pageSize: 10,
    trendMonths: 6,
    platformGrowthTrendRange: 'MONTHLY',
  });
  const debouncedSearch = useDebounce(searchTerm, 500);
  const [draftFilters, setDraftFilters] = useState({
    status: '',
    fromDate: '',
    toDate: '',
    userType: '',
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
    const fetchUserSummaryReport = async () => {
      try {
        const response: IResponse<IUserSummaryReportData> = await apiClient.get(
          API_END_POINTS.GET_USER_SUMMARY_REPORT + queryString,
        );

        if (isSuccessResponse(response.statusCode)) {
          setReportData({
            ...response.data,
            platformGrowthTrend: response.data.platformGrowthTrend || [],
            userCardInfo: response.data.userCardInfo || {
              totalMsp: 0,
              totalClientAdmin: 0,
              totalLicenseUser: 0,
              totalActiveUser: 0,
              totalSuspendedUser: 0,
            },
          });
        }
      } catch (error) {
        console.error('Error fetching user summary report:', error);
      }
    };

    fetchUserSummaryReport();
  }, [apiClient, queryString]);

  const resetToFirstPage = () => {
    setPaginationKey(prev => prev + 1);
  };

  const handleResetQueries = () => {
    setSearchTerm('');
    setQueryParams({
      ...InitGetListParams,
      search: '',
      status: '',
      userType: '',
      fromDate: '',
      toDate: '',
      offset: 0,
      pageSize: 10,
      trendMonths: 6,
      platformGrowthTrendRange: 'MONTHLY',
    });
    setDraftFilters({
      status: '',
      fromDate: '',
      toDate: '',
      userType: '',
    });
    resetToFirstPage();
  };

  const handleFilterPopoverOpenChange = (isOpen: boolean) => {
    if (isOpen) {
      setDraftFilters({
        status: queryParams.status || '',
        fromDate: queryParams.fromDate || '',
        toDate: queryParams.toDate || '',
        userType: queryParams.userType || '',
      });
    }
    setIsFilterPopoverOpen(isOpen);
  };

  const handleApplyFilters = () => {
    setQueryParams(prevState => ({
      ...prevState,
      status: draftFilters.status,
      fromDate: draftFilters.fromDate,
      toDate: draftFilters.toDate,
      userType: draftFilters.userType,
      offset: 0,
    }));
    resetToFirstPage();
    setIsFilterPopoverOpen(false);
  };

  const viewData = useMemo(() => {
    const cardInfo = reportData.userCardInfo || {
      totalMsp: 0,
      totalClientAdmin: 0,
      totalLicenseUser: 0,
      totalActiveUser: 0,
      totalSuspendedUser: 0,
    };

    return {
      totalMsp: cardInfo.totalMsp,
      totalClients: cardInfo.totalClientAdmin,
      totalLicenseUser: cardInfo.totalLicenseUser,
      activeUsers: cardInfo.totalActiveUser,
      suspendedLocked: cardInfo.totalSuspendedUser,
      platformGrowth: (reportData.platformGrowthTrend || []).map(item => ({
        label: item.label,
        totalMsp: item.totalMsp,
        totalClientAdmin: item.totalClientAdmin,
        totalLicenseUser: item.totalLicenseUser,
        totalActiveUser: item.totalActiveUser,
      })),
      users: reportData.details.items.map(item => ({
        name: item.name || '-',
        department: item.department || '-',
        email: item.email || '-',
        role: item.role || '-',
        status: item.status || '-',
        lastLogin: formateDateAndTime(item.lastLoginAt) || '-',
        riskGroup: item.riskGroup || 'HIGH_RISK',
      })),
      total: reportData.details.total,
      pageSize: reportData.details.pageSize,
    };
  }, [reportData]);

  const handlePageChange = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-5">
        <StatCard
          label="Total MSPs"
          value={viewData.totalMsp}
          icon={Building2}
          iconClassName="text-blue-400"
        />
        <StatCard
          label="Total Clients"
          value={viewData.totalClients}
          icon={Users}
          iconClassName="text-blue-400"
        />
        <StatCard
          label="Total Licenses User"
          value={viewData.totalLicenseUser}
          icon={User}
          iconClassName="text-sky-400"
        />
        <StatCard
          label="Active Users"
          value={viewData.activeUsers}
          icon={UserCheck}
          iconClassName="text-emerald-400"
        />
        <StatCard
          label="Suspended / Locked"
          value={viewData.suspendedLocked}
          icon={Lock}
          iconClassName="text-red-400"
        />
      </div>
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="flex items-center gap-2 text-base">
            Platform Growth Trend
            <Info className="size-4 text-muted-foreground" />
          </CardTitle>
          <Select
            value={queryParams.platformGrowthTrendRange || 'MONTHLY'}
            onValueChange={value =>
              setQueryParams(prevState => ({
                ...prevState,
                platformGrowthTrendRange: value as TPlatformGrowthTrendRange,
              }))
            }
          >
            <SelectTrigger className="w-[150px]">
              <SelectValue placeholder="Select range" />
            </SelectTrigger>
            <SelectContent>
              {PLATFORM_GROWTH_RANGE_OPTIONS.map(option => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={280}>
            <LineChart data={viewData.platformGrowth}>
              <CartesianGrid
                strokeDasharray="3 3"
                stroke="hsl(217, 32%, 25%)"
              />
              <XAxis
                dataKey="label"
                stroke="hsl(215, 20%, 65%)"
                fontSize={12}
              />
              <YAxis
                yAxisId="left"
                stroke="hsl(215, 20%, 65%)"
                fontSize={12}
                allowDecimals={false}
              />
              <YAxis
                yAxisId="right"
                orientation="right"
                stroke="#a855f7"
                fontSize={12}
                allowDecimals={false}
              />
              <Tooltip
                contentStyle={{
                  background: 'hsl(217, 33%, 17%)',
                  border: '1px solid hsl(217, 33%, 25%)',
                  borderRadius: 8,
                  color: '#fff',
                }}
                formatter={(value: number | undefined) =>
                  (value ?? 0).toLocaleString()
                }
              />
              <Legend />
              {PLATFORM_GROWTH_SERIES.map(series => (
                <Line
                  key={series.key}
                  yAxisId={series.yAxisId}
                  type="monotone"
                  dataKey={series.key}
                  name={series.name}
                  stroke={series.color}
                  strokeWidth={2}
                  dot={{ r: 3, fill: series.color }}
                  activeDot={{ r: 5 }}
                />
              ))}
            </LineChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardTitle className="text-base">User Details</CardTitle>
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
              placeholder="Search by name or email"
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
                      <p className="text-sm font-medium">Status</p>
                      <Select
                        value={draftFilters.status || 'all'}
                        onValueChange={value =>
                          setDraftFilters(prevState => ({
                            ...prevState,
                            status: value === 'all' ? '' : value,
                          }))
                        }
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="All status" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="all">All status</SelectItem>
                          <SelectItem value="Active">Active</SelectItem>
                          <SelectItem value="Suspended">Suspended</SelectItem>
                          <SelectItem value="Inactive">Inactive</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>

                    <div className="space-y-2">
                      <p className="text-sm font-medium">User Type (Role)</p>
                      <Input
                        value={draftFilters.userType}
                        onChange={event =>
                          setDraftFilters(prevState => ({
                            ...prevState,
                            userType: event.target.value,
                          }))
                        }
                        placeholder="e.g. Admin"
                      />
                    </div>

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
                <TableHead>Name</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Role</TableHead>
                <TableHead>Department</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Risk Group</TableHead>
                <TableHead>Last Login</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {viewData.users.map((u, i: number) => (
                <TableRow key={i}>
                  <TableCell className="font-medium">{u.name}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {u.email}
                  </TableCell>
                  <TableCell className="capitalize">
                    {u.role.toLowerCase().split('_').join(' ')}
                  </TableCell>
                  <TableCell>{u.department}</TableCell>
                  <TableCell>
                    <StatusBadge status={u.status.toLowerCase()} />
                  </TableCell>
                  <TableCell>
                    <StatusBadge riskGroup={u.riskGroup} />
                  </TableCell>
                  <TableCell>{u.lastLogin}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          <div className="flex justify-end pt-6">
            <Pagination
              key={paginationKey}
              total={viewData.total}
              perPage={viewData.pageSize}
              onPageChange={handlePageChange}
            />
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default UserSummaryReport;
