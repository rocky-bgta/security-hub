import { ChevronDown, Search } from 'lucide-react';
import { useEffect, useMemo, useRef, useState } from 'react';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import Pagination from 'common/Pagination';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import { SeverityBadge } from 'components/common/Badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'components/common/Table';
import useDebounce from 'hooks/UseDebounce';
import { useAPI } from 'hooks/UseAPI';
import type { IBreachedEmailResponse } from 'models/BreachMonitor';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime, objectToQueryString } from 'utils/Helper';

type TimeRangeOption = 'all' | '7' | '30' | '90' | 'LAST_YEAR' | 'CUSTOM';

const formatDateForApi = (dateTimeValue: string) => {
  if (!dateTimeValue) return undefined;
  const [datePart] = dateTimeValue.split('T');
  return datePart || undefined;
};

const formatDate = (date: Date) => {
  return date.toISOString().split('T')[0];
};

const getCurrentLocalDateTime = () => {
  const now = new Date();
  const localDateTime = new Date(
    now.getTime() - now.getTimezoneOffset() * 60 * 1000,
  );
  return localDateTime.toISOString().slice(0, 16);
};

const getPresetDateRange = (timeRange: Exclude<TimeRangeOption, 'CUSTOM'>) => {
  const toDate = new Date();
  const fromDate = new Date(toDate);

  if (timeRange === 'LAST_YEAR') {
    fromDate.setFullYear(fromDate.getFullYear() - 1);
  } else {
    fromDate.setDate(fromDate.getDate() - Number(timeRange));
  }

  return {
    fromDate: formatDate(fromDate),
    toDate: formatDate(toDate),
  };
};

const BreachTable = () => {
  const [breachedEmails, setBreachedEmails] = useState<
    IList<IBreachedEmailResponse>
  >({ items: [], total: 0, pageSize: 0, offset: 0 });
  const [searchText, setSearchText] = useState('');
  const [timeRange, setTimeRange] = useState<TimeRangeOption>('all');
  const [startDateTime, setStartDateTime] = useState<string>('');
  const [endDateTime, setEndDateTime] = useState<string>('');
  const [isTimeRangeOpen, setIsTimeRangeOpen] = useState(false);
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    offset: 0,
    pageSize: 10,
  });
  const timeRangeDropdownRef = useRef<HTMLDivElement | null>(null);

  const apiclient = useAPI();
  const debouncedSearchText = useDebounce(searchText.trim(), 500);
  const todayMaxDateTime = useMemo(() => getCurrentLocalDateTime(), []);

  const filterParams = useMemo<
    Pick<IGetListParams, 'email' | 'fromDate' | 'toDate'>
  >(() => {
    if (timeRange === 'all') {
      return {
        email: debouncedSearchText,
        fromDate: '',
        toDate: '',
      };
    }

    if (timeRange === 'CUSTOM') {
      return {
        email: debouncedSearchText,
        fromDate: formatDateForApi(startDateTime),
        toDate: formatDateForApi(endDateTime),
      };
    }

    const range = getPresetDateRange(timeRange);
    return {
      email: debouncedSearchText,
      fromDate: range.fromDate,
      toDate: range.toDate,
    };
  }, [debouncedSearchText, timeRange, startDateTime, endDateTime]);

  const effectiveQueryParams = useMemo(() => {
    return {
      ...queryParams,
      ...filterParams,
    } satisfies IGetListParams;
  }, [queryParams, filterParams]);

  const selectedTimeRangeLabel = useMemo(() => {
    switch (timeRange) {
      case '7':
        return 'Last 7 days';
      case '30':
        return 'Last 30 days';
      case '90':
        return 'Last 90 days';
      case 'LAST_YEAR':
        return 'Last year';
      case 'CUSTOM':
        return 'Custom';
      case 'all':
        return 'All time';
      default:
        return 'Select time range';
    }
  }, [timeRange]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        timeRangeDropdownRef.current &&
        !timeRangeDropdownRef.current.contains(event.target as Node)
      ) {
        setIsTimeRangeOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  useEffect(() => {
    if (timeRange === 'CUSTOM' && (!startDateTime || !endDateTime)) {
      return;
    }

    const fetchBreachedEmails = async () => {
      try {
        const response: IResponse<IList<IBreachedEmailResponse>> =
          await apiclient.get(
            API_END_POINTS.GET_BREACH_MONITOR_BREACHED_EMAIL_LIST +
              objectToQueryString(effectiveQueryParams),
          );

        // if (!isSuccessResponse(response.statusCode)) {
        //   throw new Error(response.message);
        // }
        setBreachedEmails(response.data);
      } catch (error) {
        console.error(
          'Error fetching breached emails:',
          (error as Error).message,
        );
      }
    };

    fetchBreachedEmails();
  }, [apiclient, effectiveQueryParams, timeRange, startDateTime, endDateTime]);

  const resetOffsetIfNeeded = () => {
    setQueryParams(prevState => {
      if (prevState.offset === 0) {
        return prevState;
      }

      return {
        ...prevState,
        offset: 0,
      };
    });
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-xl">Recent Breach Alerts</CardTitle>
          <div className="grid gap-3 md:grid-cols-2 lg:w-1/2 xl:grid-cols-3">
            <div className="relative xl:col-span-2">
              <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                id="breach-text-search"
                value={searchText}
                onChange={event => {
                  setSearchText(event.target.value);
                  resetOffsetIfNeeded();
                }}
                placeholder="Search by email..."
                className="h-9 w-full rounded-md border border-card-border bg-transparent pl-9 pr-3 text-sm"
              />
            </div>

            <div ref={timeRangeDropdownRef} className="space-y-1">
              <button
                type="button"
                onClick={() => setIsTimeRangeOpen(prevState => !prevState)}
                data-state={isTimeRangeOpen ? 'open' : 'closed'}
                className="group flex h-9 w-full items-center justify-between rounded-md border border-card-border bg-transparent px-3 text-left text-sm"
              >
                <span>{selectedTimeRangeLabel}</span>
                <ChevronDown className="size-4 text-muted-foreground transition-transform duration-200 ease-in-out group-data-[state=open]:rotate-180" />
              </button>

              {isTimeRangeOpen && (
                <div className="absolute z-30 mt-2 w-full rounded-md border border-card-border bg-card-background p-3 shadow-md">
                  <RadioGroup
                    value={timeRange}
                    onValueChange={value => {
                      const selectedValue = value as TimeRangeOption;
                      setTimeRange(selectedValue);
                      resetOffsetIfNeeded();

                      if (selectedValue !== 'CUSTOM') {
                        setStartDateTime('');
                        setEndDateTime('');
                        setIsTimeRangeOpen(false);
                      }
                    }}
                    className="space-y-2"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem id="time-range-all" value="all" />
                      <Label
                        htmlFor="time-range-all"
                        className="cursor-pointer"
                      >
                        All time
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem id="time-range-7" value="7" />
                      <Label htmlFor="time-range-7" className="cursor-pointer">
                        Last 7 days
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem id="time-range-30" value="30" />
                      <Label htmlFor="time-range-30" className="cursor-pointer">
                        Last 30 days
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem id="time-range-90" value="90" />
                      <Label htmlFor="time-range-90" className="cursor-pointer">
                        Last 90 days
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem
                        id="time-range-last-year"
                        value="LAST_YEAR"
                      />
                      <Label
                        htmlFor="time-range-last-year"
                        className="cursor-pointer"
                      >
                        Last year
                      </Label>
                    </div>
                    <div className="space-y-2">
                      <div className="flex items-center space-x-2">
                        <RadioGroupItem id="time-range-custom" value="CUSTOM" />
                        <Label
                          htmlFor="time-range-custom"
                          className="cursor-pointer"
                        >
                          Custom
                        </Label>
                      </div>

                      {timeRange === 'CUSTOM' && (
                        <div className="grid gap-3 pl-6">
                          <div className="space-y-1">
                            <Label
                              htmlFor="breach-start-date-time"
                              className="text-xs font-medium tracking-wide text-muted-foreground"
                            >
                              Time From
                            </Label>
                            <Input
                              id="breach-start-date-time"
                              type="datetime-local"
                              value={startDateTime}
                              max={todayMaxDateTime}
                              onChange={event => {
                                const newStartDate = event.target.value;

                                if (newStartDate > todayMaxDateTime) {
                                  return;
                                }

                                setStartDateTime(newStartDate);
                                resetOffsetIfNeeded();

                                if (
                                  endDateTime &&
                                  newStartDate &&
                                  endDateTime < newStartDate
                                ) {
                                  setEndDateTime('');
                                }

                                event.currentTarget.blur();
                              }}
                              className="h-9 w-full cursor-pointer rounded-md border border-card-border bg-transparent px-3 text-sm"
                            />
                          </div>

                          <div className="space-y-1">
                            <Label
                              htmlFor="breach-end-date-time"
                              className="text-xs font-medium tracking-wide text-muted-foreground"
                            >
                              Time To
                            </Label>
                            <Input
                              id="breach-end-date-time"
                              type="datetime-local"
                              value={endDateTime}
                              min={startDateTime || undefined}
                              max={todayMaxDateTime}
                              disabled={!startDateTime}
                              onChange={event => {
                                const newEndDate = event.target.value;

                                if (
                                  (startDateTime &&
                                    newEndDate < startDateTime) ||
                                  newEndDate > todayMaxDateTime
                                )
                                  return;

                                setEndDateTime(newEndDate);
                                resetOffsetIfNeeded();

                                event.currentTarget.blur();
                                setIsTimeRangeOpen(false);
                              }}
                              className="h-9 w-full cursor-pointer rounded-md border border-card-border bg-transparent px-3 text-sm disabled:cursor-not-allowed disabled:opacity-60"
                            />
                          </div>
                        </div>
                      )}
                    </div>
                  </RadioGroup>
                </div>
              )}
            </div>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {breachedEmails.total === 0 && (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <div className="mb-4 rounded-full bg-muted p-4">
              <Search className="size-8 text-muted-foreground" />
            </div>
            <h3 className="text-lg font-semibold">No breaches detected</h3>
            <p className="mt-1 max-w-sm text-sm text-muted-foreground">
              No breaches detected for the last 30 days.
            </p>
          </div>
        )}
        {breachedEmails.total > 0 && (
          <Table className="border-0">
            <TableHeader className="bg-transparent">
              <TableRow>
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Echoes
                </TableHead>

                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Breach Date
                </TableHead>

                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Ingestion Date
                </TableHead>

                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Domain
                </TableHead>
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Email
                </TableHead>
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Password/Hash
                </TableHead>
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Database Name
                </TableHead>

                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Found In
                </TableHead>

                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Source
                </TableHead>
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Severity
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {breachedEmails.items.map(b => (
                <TableRow key={b.id} className="hover:bg-muted/50">
                  <TableCell className="p-3">
                    {b.echoesCount === 0 ? (
                      <span className="text-xs text-muted-foreground">
                        No echoes
                      </span>
                    ) : (
                      <span className="inline-flex items-center rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
                        {b.echoesCount}{' '}
                        {b.echoesCount === 1 ? 'Echo' : 'Echoes'}
                      </span>
                    )}
                  </TableCell>

                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {formateDateAndTime(b.firstSeenAt)}
                  </TableCell>

                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {b.lastSeenAt
                      ? formateDateAndTime(b.lastSeenAt)
                      : formateDateAndTime(b.firstSeenAt)}
                  </TableCell>

                  <TableCell className="p-3 text-xs">{b.domain}</TableCell>
                  <TableCell className="p-3 font-medium">{b.email}</TableCell>
                  <TableCell className="p-3 font-mono text-xs">
                    {b.password ? (
                      b.password
                    ) : (
                      <span className="text-muted-foreground">N/A</span>
                    )}
                  </TableCell>
                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {b.databaseName || '-'}
                  </TableCell>

                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {b.foundIn || '-'}
                  </TableCell>

                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {b.source}
                  </TableCell>
                  <TableCell className="p-3">
                    <SeverityBadge severity={b.breachSeverity} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}

        {breachedEmails?.total > 0 && (
          <div className="my-6 flex justify-end">
            <Pagination
              total={breachedEmails?.total || 0}
              perPage={breachedEmails?.pageSize || 0}
              onPageChange={onPageChangeHandler}
            />
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default BreachTable;
