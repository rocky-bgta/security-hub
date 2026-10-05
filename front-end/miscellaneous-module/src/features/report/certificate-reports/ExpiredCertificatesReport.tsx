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

import { Filter } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';

import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import {
  IExpiredCertificateReportList,
  IExpiredCertificateReportSummary,
} from 'models/CertificateReports';
import { IGetListParams, IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  formateDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

interface IExpiredCertificateQueryParams extends IGetListParams {
  fromDate?: string;
  toDate?: string;
}

const formatStatusLabel = (status: string) => {
  const label = status.toLowerCase().replace(/_/g, ' ');
  return label.charAt(0).toUpperCase() + label.slice(1);
};

const StatusBadge = ({ status }: { status: string }) => {
  const variants: Record<string, string> = {
    valid: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    expired: 'bg-red-500/20 text-red-400 border-red-500/30',
    expiring_soon: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    'expiring soon': 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
  };
  const cls =
    variants[status.toLowerCase().replace(/_/g, ' ')] ||
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

const daysCellClass = (days: string) => {
  const normalized = days.toLowerCase();
  if (normalized.includes('overdue') || normalized.includes('expired')) {
    return 'text-destructive';
  }
  return 'text-yellow-400';
};

const ExpiredCertificatesReport = () => {
  const apiClient = useAPI();
  const [summary, setSummary] = useState<IExpiredCertificateReportSummary>({
    totalCertificates: 0,
    totalValidCertificates: 0,
    totalExpiredCertificates: 0,
    totalExpiringCertificates: 0,
  });
  const [reportList, setReportList] = useState<IExpiredCertificateReportList>({
    items: [],
    total: 0,
    pageSize: 10,
    offset: 0,
  });
  const [paginationKey, setPaginationKey] = useState(0);
  const [isFilterPopoverOpen, setIsFilterPopoverOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [queryParams, setQueryParams] =
    useState<IExpiredCertificateQueryParams>({
      ...InitGetListParams,
      search: '',
      status: '',
      fromDate: '',
      toDate: '',
      offset: 0,
      pageSize: 10,
    });
  const debouncedSearch = useDebounce(searchTerm, 500);
  const [draftFilters, setDraftFilters] = useState({
    status: '',
    fromDate: '',
    toDate: '',
  });

  const effectiveQueryParams = useMemo(
    () => ({ ...queryParams, search: debouncedSearch }),
    [debouncedSearch, queryParams],
  );

  const listQueryString = useMemo(
    () => objectToQueryString(effectiveQueryParams),
    [effectiveQueryParams],
  );

  const summaryQueryString = useMemo(
    () =>
      objectToQueryString({
        fromDate: queryParams.fromDate,
        toDate: queryParams.toDate,
      } as IGetListParams),
    [queryParams.fromDate, queryParams.toDate],
  );

  useEffect(() => {
    const fetchExpiredCertificatesSummary = async () => {
      try {
        const response: IResponse<IExpiredCertificateReportSummary> =
          await apiClient.get(
            API_END_POINTS.GET_EXPIRED_CERTIFICATES_REPORT_SUMMARY +
              summaryQueryString,
          );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'API error');
        }

        setSummary({
          totalCertificates: response.data.totalCertificates ?? 0,
          totalValidCertificates: response.data.totalValidCertificates ?? 0,
          totalExpiredCertificates:
            response.data.totalExpiredCertificates ?? 0,
          totalExpiringCertificates:
            response.data.totalExpiringCertificates ?? 0,
        });
      } catch (error) {
        console.error(
          'Error fetching expired certificates report summary:',
          error,
        );
      }
    };

    fetchExpiredCertificatesSummary();
  }, [apiClient, summaryQueryString]);

  useEffect(() => {
    const fetchExpiredCertificatesReport = async () => {
      try {
        const response: IResponse<IExpiredCertificateReportList> =
          await apiClient.get(
            API_END_POINTS.GET_EXPIRED_CERTIFICATES_REPORT + listQueryString,
          );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'API error');
        }

        setReportList({
          items: response.data.items || [],
          total: response.data.total ?? 0,
          pageSize: response.data.pageSize ?? 10,
          offset: response.data.offset ?? 0,
        });
      } catch (error) {
        console.error('Error fetching expired certificates report:', error);
      }
    };

    fetchExpiredCertificatesReport();
  }, [apiClient, listQueryString]);

  const resetToFirstPage = () => {
    setPaginationKey(prev => prev + 1);
  };

  const handleResetQueries = () => {
    setSearchTerm('');
    setQueryParams({
      ...InitGetListParams,
      search: '',
      status: '',
      fromDate: '',
      toDate: '',
      offset: 0,
      pageSize: 10,
    });
    setDraftFilters({
      status: '',
      fromDate: '',
      toDate: '',
    });
    resetToFirstPage();
  };

  const handleFilterPopoverOpenChange = (isOpen: boolean) => {
    if (isOpen) {
      setDraftFilters({
        status: queryParams.status || '',
        fromDate: queryParams.fromDate || '',
        toDate: queryParams.toDate || '',
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
        <StatCard label="Total Certificates" value={summary.totalCertificates} />
        <StatCard
          label="Valid"
          value={summary.totalValidCertificates}
          color="text-emerald-400"
        />
        <StatCard
          label="Expired"
          value={summary.totalExpiredCertificates}
          color="text-destructive"
        />
        <StatCard
          label="Expiring Soon"
          value={summary.totalExpiringCertificates}
          color="text-yellow-400"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Certificate Renewal Tracker</CardTitle>
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
              placeholder="Search by cert ID, user, or course"
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
                    <div className="space-y-2 md:col-span-2">
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
                          <SelectItem value="VALID">Valid</SelectItem>
                          <SelectItem value="EXPIRING_SOON">
                            Expiring Soon
                          </SelectItem>
                          <SelectItem value="EXPIRED">Expired</SelectItem>
                        </SelectContent>
                      </Select>
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
                <TableHead>Cert ID</TableHead>
                <TableHead>User</TableHead>
                <TableHead>Course</TableHead>
                <TableHead>Expiry Date</TableHead>
                <TableHead>Days</TableHead>
                <TableHead>Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {!reportList.items.length ? (
                <TableRow>
                  <TableCell
                    colSpan={6}
                    className="py-8 text-center text-muted-foreground"
                  >
                    No data available
                  </TableCell>
                </TableRow>
              ) : (
                reportList.items.map((certificate, index) => (
                  <TableRow key={`${certificate.certificateId}-${index}`}>
                    <TableCell className="font-medium">
                      {certificate.certificateId}
                    </TableCell>
                    <TableCell>{certificate.user}</TableCell>
                    <TableCell>{certificate.course}</TableCell>
                    <TableCell>
                      {formateDateAndTime(certificate.expiryDate)}
                    </TableCell>
                    <TableCell className={daysCellClass(certificate.days)}>
                      {certificate.days}
                    </TableCell>
                    <TableCell>
                      <StatusBadge status={certificate.certificateStatus} />
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>

          <div className="flex justify-end pt-6">
            <Pagination
              key={paginationKey}
              total={reportList.total}
              perPage={reportList.pageSize}
              onPageChange={handlePageChange}
            />
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ExpiredCertificatesReport;
