import { Eye, Search } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { DateRange } from 'react-day-picker';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader } from 'common/Card';
import Pagination from 'common/Pagination';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
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
import { DateFilter } from 'components/DateFilter';
import UserTableLoader from 'components/skeleton/UserTableLoader';
import { format } from 'date-fns';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { Calendar, Filter } from 'lucide-react';
import { IGetListParams, IList, UserActivityType } from 'models/Global';
import { IActivityLogItem } from 'models/User';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  cn,
  formatDateAndTime,
  fromDateRange,
  humanizeText,
  isSuccessResponse,
  parseLocalDateString,
  toDateRange,
  toLocalDateString,
} from 'utils/Helper';

interface IActivityLogParams extends IGetListParams {
  activityType?: string;
  activityStatus?: string;
}

const UserActivityLogs = () => {
  const apiClient = useAPI();
  const [logs, setLogs] = useState<IList<IActivityLogItem>>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [isLoading, setIsLoading] = useState(true);
  const [queryParams, setQueryParams] = useState<IActivityLogParams>({
    ...InitGetListParams,
    search: '',
    activityType: '',
    activityStatus: '',
    startDate: '',
    endDate: '',
  });

  const searchDebounce = useDebounce(queryParams.search, 500);
  const [dateRange, setDateRange] = useState<DateRange | undefined>();

  const pickerStart =
    dateRange?.from ??
    (queryParams.startDate
      ? parseLocalDateString(queryParams.startDate)
      : undefined);
  const pickerEnd =
    dateRange?.to ??
    (queryParams.endDate
      ? parseLocalDateString(queryParams.endDate)
      : undefined);

  useEffect(() => {
    if (searchDebounce !== undefined) {
      fetchLogs();
    }
  }, [
    searchDebounce,
    queryParams.offset,
    queryParams.activityType,
    queryParams.activityStatus,
    queryParams.startDate,
    queryParams.endDate,
  ]);

  const fetchLogs = async () => {
    try {
      setIsLoading(true);
      const payload = {
        search: queryParams.search,
        activityType: queryParams.activityType || undefined,
        activityStatus: queryParams.activityStatus || undefined,
        startDate: queryParams.startDate
          ? parseLocalDateString(queryParams.startDate).toISOString()
          : undefined,
        endDate: queryParams.endDate
          ? parseLocalDateString(queryParams.endDate).toISOString()
          : undefined,
        offset: queryParams.offset,
        pageSize: queryParams.pageSize,
      };

      const response: any = await apiClient.post(
        API_END_POINTS.ACTIVITY_LOG_LIST,
        { data: payload },
      );
      if (isSuccessResponse(response.statusCode)) {
        setLogs({
          offset: response.data.currentPage,
          pageSize: response.data.pageSize,
          total: response.data.totalElements,
          items: response.data.activityLogs,
        });
      }
    } catch (error) {
      console.error('Error fetching activity logs:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const getStatusVariant = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'SUCCESS':
        return 'bg-[#22c55e] text-[#f8fafc]';
      case 'FAILED':
        return 'bg-[#ef4444] text-[#f8fafc]';
      default:
        return 'bg-muted text-muted-foreground';
    }
  };

  return (
    <div className="space-y-4 sm:space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold sm:text-2xl">Activity Logs</h2>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="!mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4 md:grid-cols-3 lg:grid-cols-6">
            <div className="relative col-span-1 sm:col-span-2">
              <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search activity logs"
                value={queryParams.search}
                onChange={e =>
                  setQueryParams({ ...queryParams, search: e.target.value })
                }
                className="pl-9"
              />
            </div>
            <Select
              value={queryParams.activityType}
              onValueChange={value =>
                setQueryParams({
                  ...queryParams,
                  activityType: value === 'all' ? '' : value,
                })
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Action Type" />
              </SelectTrigger>
              <SelectContent className="max-h-60">
                <SelectItem value="all">All Types</SelectItem>
                {Object.values(UserActivityType).map(action => (
                  <SelectItem key={action} value={action}>
                    {humanizeText(action)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select
              value={queryParams.activityStatus}
              onValueChange={value =>
                setQueryParams({
                  ...queryParams,
                  activityStatus: value === 'all' ? '' : value,
                })
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="SUCCESS">Success</SelectItem>
                <SelectItem value="FAILED">Failed</SelectItem>
              </SelectContent>
            </Select>

            <Popover>
              <PopoverTrigger asChild>
                <Button
                  variant="outline"
                  className={cn(
                    'w-full justify-start text-left font-normal',
                    !pickerStart && 'text-muted-foreground',
                  )}
                >
                  <Calendar className="mr-2 size-4" />
                  {pickerStart ? (
                    pickerEnd ? (
                      <>
                        {format(pickerStart, 'LLL dd')} -{' '}
                        {format(pickerEnd, 'LLL dd')}
                      </>
                    ) : (
                      format(pickerStart, 'LLL dd, y')
                    )
                  ) : (
                    <span>Select a date</span>
                  )}
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto p-0" align="start">
                <DateFilter
                  mode="range"
                  defaultMonth={pickerStart}
                  selected={
                    dateRange ??
                    toDateRange(queryParams.startDate, queryParams.endDate)
                  }
                  onSelect={value => {
                    if (!value?.from) {
                      setDateRange(undefined);
                      setQueryParams(prev => ({
                        ...prev,
                        startDate: '',
                        endDate: '',
                        offset: 0,
                      }));
                      return;
                    }

                    if (
                      value.to &&
                      toLocalDateString(value.from) ===
                        toLocalDateString(value.to) &&
                      !dateRange?.from
                    ) {
                      setDateRange({ from: value.from, to: undefined });
                      return;
                    }

                    if (!value.to) {
                      setDateRange({ from: value.from, to: undefined });
                      return;
                    }

                    setDateRange(value);
                    setQueryParams(prev => ({
                      ...prev,
                      ...fromDateRange(value),
                      offset: 0,
                    }));
                  }}
                  numberOfMonths={2}
                  className={cn('pointer-events-auto p-3')}
                />
              </PopoverContent>
            </Popover>

            <Button
              variant="outline"
              className="w-full"
              onClick={() => {
                setDateRange(undefined);
                setQueryParams({
                  ...queryParams,
                  activityType: '',
                  activityStatus: '',
                  startDate: '',
                  endDate: '',
                  search: '',
                });
              }}
            >
              <Filter className="mr-2 size-4" />
              Clear Filters
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <UserTableLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Activity Type</TableHead>
                  <TableHead>User Email</TableHead>
                  <TableHead>Timestamp</TableHead>
                  <TableHead>User Type</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {logs?.items?.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={6}
                      className="text-center text-muted-foreground"
                    >
                      No activity logs found.
                    </TableCell>
                  </TableRow>
                ) : (
                  logs?.items?.map(log => (
                    <TableRow key={log.activityId}>
                      <TableCell className="font-medium">
                        {humanizeText(log.activityType)}
                      </TableCell>
                      <TableCell>{log.userEmail}</TableCell>
                      <TableCell>{formatDateAndTime(log.timestamp)}</TableCell>
                      <TableCell>{humanizeText(log.userType)}</TableCell>
                      <TableCell>
                        <Badge className={getStatusVariant(log.activityStatus)}>
                          {log.activityStatus}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Dialog>
                          <DialogTrigger asChild>
                            <Button variant="outline" size="sm">
                              <Eye className="mr-1 size-4" />
                              View Details
                            </Button>
                          </DialogTrigger>
                          <DialogContent className="max-w-2xl">
                            <DialogHeader>
                              <DialogTitle>Activity Details</DialogTitle>
                            </DialogHeader>
                            <div className="space-y-4">
                              <div className="grid grid-cols-2 gap-4">
                                <div>
                                  <Label className="text-sm font-medium">
                                    Activity Type
                                  </Label>
                                  <p className="text-sm text-foreground">
                                    {humanizeText(log.activityType)}
                                  </p>
                                </div>
                                <div>
                                  <Label className="text-sm font-medium">
                                    User Email
                                  </Label>
                                  <p className="text-sm text-foreground">
                                    {log.userEmail}
                                  </p>
                                </div>
                                <div>
                                  <Label className="text-sm font-medium">
                                    Timestamp
                                  </Label>
                                  <p className="text-sm text-foreground">
                                    {formatDateAndTime(log.timestamp)}
                                  </p>
                                </div>
                                <div>
                                  <Label className="text-sm font-medium">
                                    User Type
                                  </Label>
                                  <p className="text-sm text-foreground">
                                    {humanizeText(log.userType)}
                                  </p>
                                </div>
                                <div>
                                  <Label className="text-sm font-medium">
                                    Status
                                  </Label>
                                  <Badge
                                    className={getStatusVariant(
                                      log.activityStatus,
                                    )}
                                  >
                                    {log.activityStatus}
                                  </Badge>
                                </div>
                                <div>
                                  <Label className="text-sm font-medium">
                                    IP Address
                                  </Label>
                                  <p className="text-sm text-foreground">
                                    {log.ipAddress}
                                  </p>
                                </div>
                              </div>

                              <div>
                                <Label className="text-sm font-medium">
                                  Description
                                </Label>
                                <p className="text-sm text-foreground">
                                  {log.activityDescription || 'N/A'}
                                </p>
                              </div>

                              {log.oldValue && log.newValue && (
                                <div className="grid grid-cols-2 gap-4">
                                  <div>
                                    <Label className="text-sm font-medium">
                                      Old Value
                                    </Label>
                                    <p className="rounded bg-muted/50 p-2 font-mono text-sm text-muted-foreground">
                                      {log.oldValue}
                                    </p>
                                  </div>
                                  <div>
                                    <Label className="text-sm font-medium">
                                      New Value
                                    </Label>
                                    <p className="rounded bg-muted/50 p-2 font-mono text-sm text-muted-foreground">
                                      {log.newValue}
                                    </p>
                                  </div>
                                </div>
                              )}
                            </div>
                          </DialogContent>
                        </Dialog>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>
        <div className="mt-4 p-4">
          <Pagination
            total={logs?.total}
            perPage={logs.pageSize}
            onPageChange={onPageChangeHandler}
          />
        </div>
      </Card>
    </div>
  );
};

export default UserActivityLogs;
