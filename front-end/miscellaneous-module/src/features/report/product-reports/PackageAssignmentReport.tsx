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
import { IGetListParams, IResponse } from 'models/Global';
import { IPackageAssignmentReportData } from 'models/PackageAssignmentReport';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';

interface IPackageAssignmentQueryParams extends IGetListParams {
  fromDate?: string;
  toDate?: string;
}

const StatusBadge = ({ status }: { status?: string }) => {
  const variants: Record<string, string> = {
    active: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    completed: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    complete: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    expired: 'bg-red-500/20 text-red-400 border-red-500/30',
    expiring: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    expiring_soon: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
  };
  const normalized = (status || '').toLowerCase().replace(/\s+/g, '_');
  const cls =
    variants[normalized] || 'bg-muted text-muted-foreground border-border';

  return (
    <Badge variant="outline" className={`${cls} text-xs capitalize`}>
      {status?.replace(/_/g, ' ')}
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

const PackageAssignmentReport = () => {
  const apiClient = useAPI();
  const [reportData, setReportData] = useState<IPackageAssignmentReportData>({
    summary: {
      totalPackages: 0,
      totalSubPackages: 0,
      activeAssignments: 0,
      expiringSoon: 0,
      expired: 0,
      completeAssignments: 0,
    },
    assignmentLog: {
      items: [],
      total: 0,
      pageSize: 10,
      offset: 0,
    },
  });
  const [paginationKey, setPaginationKey] = useState(0);
  const [isFilterPopoverOpen, setIsFilterPopoverOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [queryParams, setQueryParams] = useState<IPackageAssignmentQueryParams>(
    {
      ...InitGetListParams,
      search: '',
      status: '',
      fromDate: '',
      toDate: '',
      offset: 0,
      pageSize: 10,
    },
  );
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

  const queryString = useMemo(
    () => objectToQueryString(effectiveQueryParams),
    [effectiveQueryParams],
  );

  useEffect(() => {
    const fetchPackageAssignmentReport = async () => {
      try {
        const response: IResponse<IPackageAssignmentReportData> =
          await apiClient.get(
            API_END_POINTS.GET_PACKAGE_ASSIGNMENT_REPORT + queryString,
          );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'API error');
        }

        setReportData(response.data);
      } catch (error) {
        console.error('Error fetching package assignment report:', error);
      }
    };

    fetchPackageAssignmentReport();
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

  const viewData = useMemo(
    () => ({
      totalPackages: reportData.summary.totalPackages,
      totalSubPackages: reportData.summary.totalSubPackages,
      activeAssignments: reportData.summary.activeAssignments,
      expiringSoon: reportData.summary.expiringSoon,
      expired: reportData.summary.expired,
      completeAssignments: reportData.summary.completeAssignments,
      assignments: reportData.assignmentLog.items.map(item => ({
        user: item.user || '-',
        packageName: item.packageName || '-',
        subPackageName: item.subPackageName || '-',
        assignedDate: item.assignedDate || '-',
        expiryDate: item.expiryDate || '-',
        status: item.status || '-',
        assignedBy: item.assignedBy || '-',
      })),
      total: reportData.assignmentLog.total,
      pageSize: reportData.assignmentLog.pageSize,
    }),
    [reportData],
  );

  const handlePageChange = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-3 xl:grid-cols-6">
        <StatCard label="Total Packages" value={viewData.totalPackages} />
        <StatCard
          label="Total Sub-Packages"
          value={viewData.totalSubPackages}
        />
        <StatCard
          label="Active Assignments"
          value={viewData.activeAssignments}
          color="text-emerald-400"
        />
        <StatCard
          label="Expiring Soon"
          value={viewData.expiringSoon}
          color="text-yellow-400"
        />
        <StatCard
          label="Expired"
          value={viewData.expired}
          color="text-destructive"
        />
        <StatCard
          label="Complete Assignments"
          value={viewData.completeAssignments}
          color="text-primary"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Assignment Log</CardTitle>
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
              placeholder="Search by user, package, or assigned by"
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
                          <SelectItem value="Active">Active</SelectItem>
                          <SelectItem value="Expiring">Expiring</SelectItem>
                          <SelectItem value="Expired">Expired</SelectItem>
                          <SelectItem value="Complete">Complete</SelectItem>
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
                <TableHead>User</TableHead>
                <TableHead>Package</TableHead>
                <TableHead>Sub-Package</TableHead>
                <TableHead>Assigned</TableHead>
                <TableHead>Expiry</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Assigned By</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {!viewData.assignments.length ? (
                <TableRow>
                  <TableCell
                    colSpan={7}
                    className="py-8 text-center text-muted-foreground"
                  >
                    No data available
                  </TableCell>
                </TableRow>
              ) : (
                viewData.assignments.map((assignment, index) => (
                  <TableRow
                    key={`${assignment.user}-${assignment.packageName}-${index}`}
                  >
                    <TableCell className="font-medium">
                      {assignment.user}
                    </TableCell>
                    <TableCell>{assignment.packageName}</TableCell>
                    <TableCell>{assignment.subPackageName}</TableCell>
                    <TableCell>{assignment.assignedDate}</TableCell>
                    <TableCell>{assignment.expiryDate}</TableCell>
                    <TableCell>
                      <StatusBadge status={assignment.status} />
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {assignment.assignedBy}
                    </TableCell>
                  </TableRow>
                ))
              )}
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

export default PackageAssignmentReport;
