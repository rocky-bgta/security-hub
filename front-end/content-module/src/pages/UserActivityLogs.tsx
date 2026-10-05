import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
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
import { DateFilter } from 'components/DateFilter';
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import UserHeading from 'components/UserHeading';
import { format } from 'date-fns';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { Calendar, Eye, Filter, Search } from 'lucide-react';
import { IGetListParams, IList, UserActivityType } from 'models/Global';
import { IActivityLogItem } from 'models/Users';
import { useEffect, useState } from 'react';
import type { DateRange } from 'react-day-picker';
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
        return 'content-bg-[#22c55e] content-text-[#f8fafc]';
      case 'FAILED':
        return 'content-bg-[#ef4444] content-text-[#f8fafc]';
      default:
        return 'content-bg-muted content-text-muted-foreground';
    }
  };

  return (
    <div className="content-space-y-4 sm:content-space-y-6">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <UserHeading variant="title" text="Activity Logs" />
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="content-flex content-items-center content-justify-between">
            <CardTitle>
              <UserHeading variant="subtitle" text="Activity Logs" />
            </CardTitle>
          </div>
          <div className="!content-mt-4 content-grid content-grid-cols-1 content-gap-3 sm:content-grid-cols-2 sm:content-gap-4 md:content-grid-cols-3 lg:content-grid-cols-6">
            <div className="content-col-span-1 content-relative sm:content-col-span-2">
              <Search className="content-absolute content-left-3 content-top-1/2 content-h-4 content-w-4 -content-translate-y-1/2 content-text-muted-foreground" />
              <Input
                placeholder="Search activity logs"
                value={queryParams.search}
                onChange={e =>
                  setQueryParams({ ...queryParams, search: e.target.value })
                }
                className="content-pl-9"
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
              <SelectContent className="content-max-h-60">
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
                    'content-w-full content-justify-start content-text-left content-font-normal',
                    !pickerStart && 'content-text-muted-foreground',
                  )}
                >
                  <Calendar className="content-mr-2 content-h-4 content-w-4" />
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
              <PopoverContent
                className="content-w-auto content-p-0"
                align="start"
              >
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
                  className={cn('content-pointer-events-auto content-p-3')}
                />
              </PopoverContent>
            </Popover>

            <Button
              variant="outline"
              className="content-w-full"
              onClick={() => {
                setDateRange(undefined);
                setQueryParams({
                  ...queryParams,
                  activityType: '',
                  activityStatus: '',
                  startDate: '',
                  endDate: '',
                });
              }}
            >
              <Filter className="content-mr-2 content-h-4 content-w-4" />
              Clear Filters
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <AssignedPackagesLoader />
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
                      className="content-text-center content-text-muted-foreground"
                    >
                      No activity logs found.
                    </TableCell>
                  </TableRow>
                ) : (
                  logs?.items?.map(log => (
                    <TableRow key={log.activityId}>
                      <TableCell className="content-font-medium">
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
                              <Eye className="content-mr-1 content-h-4 content-w-4" />
                              View Details
                            </Button>
                          </DialogTrigger>
                          <DialogContent className="content-max-w-2xl">
                            <DialogHeader>
                              <DialogTitle>Activity Details</DialogTitle>
                            </DialogHeader>
                            <div className="content-space-y-4">
                              <div className="content-grid content-grid-cols-2 content-gap-4">
                                <div>
                                  <Label className="content-text-sm content-font-medium">
                                    Activity Type
                                  </Label>
                                  <p className="content-text-sm content-text-foreground">
                                    {humanizeText(log.activityType)}
                                  </p>
                                </div>
                                <div>
                                  <Label className="content-text-sm content-font-medium">
                                    User Email
                                  </Label>
                                  <p className="content-text-sm content-text-foreground">
                                    {log.userEmail}
                                  </p>
                                </div>
                                <div>
                                  <Label className="content-text-sm content-font-medium">
                                    Timestamp
                                  </Label>
                                  <p className="content-text-sm content-text-foreground">
                                    {formatDateAndTime(log.timestamp)}
                                  </p>
                                </div>
                                <div>
                                  <Label className="content-text-sm content-font-medium">
                                    User Type
                                  </Label>
                                  <p className="content-text-sm content-text-foreground">
                                    {humanizeText(log.userType)}
                                  </p>
                                </div>
                                <div>
                                  <Label className="content-text-sm content-font-medium">
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
                                  <Label className="content-text-sm content-font-medium">
                                    IP Address
                                  </Label>
                                  <p className="content-text-sm content-text-foreground">
                                    {log.ipAddress}
                                  </p>
                                </div>
                              </div>

                              <div>
                                <Label className="content-text-sm content-font-medium">
                                  Description
                                </Label>
                                <p className="content-text-sm content-text-foreground">
                                  {log.activityDescription || 'N/A'}
                                </p>
                              </div>

                              {log.oldValue && log.newValue && (
                                <div className="content-grid content-grid-cols-2 content-gap-4">
                                  <div>
                                    <Label className="content-text-sm content-font-medium">
                                      Old Value
                                    </Label>
                                    <p className="content-rounded content-bg-muted/50 content-p-2 content-font-mono content-text-sm content-text-muted-foreground">
                                      {log.oldValue}
                                    </p>
                                  </div>
                                  <div>
                                    <Label className="content-text-sm content-font-medium">
                                      New Value
                                    </Label>
                                    <p className="content-rounded content-bg-muted/50 content-p-2 content-font-mono content-text-sm content-text-muted-foreground">
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
        <div className="content-mt-4 content-p-4">
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
