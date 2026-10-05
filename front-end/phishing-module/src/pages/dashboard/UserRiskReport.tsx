import { Button } from 'common/Button';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
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
import { Skeleton } from 'components/LoadingSkeleton';
import IconBackButton from 'components/IconBackButton';
import { ExportReportModal } from 'features/dashboard/ExportReportModal';
import { useAPI } from 'hooks/UseAPI';
import { useReports } from 'hooks/UseReports';
import { Download, RotateCcw, Search } from 'lucide-react';
import {
  IUserRiskSummary,
  RiskLevel,
  getRiskLevelColor,
  getRiskLevelLabel,
} from 'models/Dashboard';
import { IGetListParams } from 'models/Global';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import {
  toCampaignChannel,
  getSimulationCopy,
  getSimulationPaths,
  type SimulationChannel,
} from 'utils/SimulationChannel';

const UserRiskReport = ({
  channel = 'phishing',
}: {
  channel?: SimulationChannel;
}) => {
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = location.state as {
    fromReports?: boolean;
    channel?: SimulationChannel;
  } | null;
  const fromReports = Boolean(locationState?.fromReports);
  const routeChannel = locationState?.channel || channel;
  const copy = getSimulationCopy(routeChannel);
  const apiClient = useAPI();
  const [searchParams, setSearchParams] = useSearchParams();
  const {
    loading,
    exporting,
    fetchUserRiskReport,
    exportUserRiskReport,
    downloadExport,
  } = useReports(toCampaignChannel(routeChannel));

  // State
  const [users, setUsers] = useState<IUserRiskSummary[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    offset: 0,
    pageSize: 10,
  });
  const [searchTerm, setSearchTerm] = useState('');
  const [departmentFilter, setDepartmentFilter] = useState('ALL');
  const [departmentList, setDepartmentList] = useState<string[]>([]);
  // const [showRepeatOffenders, setShowRepeatOffenders] = useState(false);
  const [exportModalOpen, setExportModalOpen] = useState(false);

  const riskLevelFilter = useMemo<RiskLevel | 'ALL'>(() => {
    const riskLevel = searchParams.get('riskLevel');
    return riskLevel &&
      Object.values(RiskLevel).includes(riskLevel as RiskLevel)
      ? (riskLevel as RiskLevel)
      : 'ALL';
  }, [searchParams]);

  // Load users
  const fetchDepartmentList = useCallback(async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.DEPARTMENT_LIST +
          'active=true&isSystemDefined=true&pageSize=1000',
      );

      if (isSuccessResponse(response.statusCode)) {
        const departments = (response.data?.items || [])
          .map((dept: { name?: string }) => dept.name)
          .filter((name: string | undefined): name is string => Boolean(name));
        setDepartmentList(departments);
      }
    } catch (error) {
      console.error('Error fetching department list:', error);
      setDepartmentList([]);
    }
  }, [apiClient]);

  const loadUsers = useCallback(async () => {
    // if (showRepeatOffenders) {
    //   const offenders = await fetchRepeatOffenders(100);
    //   setUsers(offenders);
    //   setTotalCount(offenders.length);
    // } else {
    const result = await fetchUserRiskReport({
      offset: queryParams.offset || 0,
      pageSize: queryParams.pageSize || 2,
      search: searchTerm || undefined,
      riskLevel: riskLevelFilter === 'ALL' ? undefined : riskLevelFilter,
      department: departmentFilter === 'ALL' ? undefined : departmentFilter,
      sortBy: 'riskScore',
      sortOrder: 'desc',
    });
    setUsers(result.data);
    setTotalCount(result.totalCount);
    // }
  }, [
    fetchUserRiskReport,
    queryParams.offset,
    queryParams.pageSize,
    searchTerm,
    riskLevelFilter,
    departmentFilter,
    // showRepeatOffenders,
  ]);

  useEffect(() => {
    setTimeout(() => {
      void fetchDepartmentList();
    }, 0);
  }, [fetchDepartmentList]);

  useEffect(() => {
    const timer = setTimeout(() => {
      void loadUsers();
    }, 0);
    return () => clearTimeout(timer);
  }, [loadUsers]);

  // Handle filter change
  const handleRiskLevelChange = (level: RiskLevel | 'ALL') => {
    setQueryParams(prev => ({ ...prev, offset: 0 }));
    if (level && level !== 'ALL') {
      setSearchParams({ riskLevel: level }, { state: location.state });
    } else {
      setSearchParams({}, { state: location.state });
    }
  };

  const handleDepartmentChange = (department: string) => {
    setQueryParams(prev => ({ ...prev, offset: 0 }));
    setDepartmentFilter(department);
  };

  const handleReset = () => {
    setDepartmentFilter('ALL');
    setSearchTerm('');
    setQueryParams(prev => ({ ...prev, offset: 0 }));
    handleRiskLevelChange('ALL');
  };

  // Handle export
  const handleExport = async (format: 'pdf' | 'excel' | 'csv') => {
    const blob = await exportUserRiskReport(format);
    if (blob) {
      const filename = `user-risk-report.${format === 'excel' ? 'xlsx' : format}`;
      downloadExport(blob, filename);
    }
    setExportModalOpen(false);
  };

  const currentPageIndex = (queryParams.offset || 0) + 1;

  const handlePageChange = (page: number) => {
    setQueryParams(prev => ({
      ...prev,
      offset: page - 1,
    }));
  };


  return (
    <div className="min-h-screen">
      {/* Header */}
      <div className="mb-4 space-y-3">
        {fromReports && (
          <IconBackButton
            onClick={() => navigate(getSimulationPaths(routeChannel).reports)}
            label="Back to Reports"
          />
        )}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-foreground">
              User Risk Report
            </h1>
            <p className="text-sm text-muted-foreground">
              Identify and track high-risk users
            </p>
          </div>
          <button
            onClick={() => setExportModalOpen(true)}
            className="hover: inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm"
          >
            <Download className="mr-2 size-4" />
            <span>Export Report</span>
          </button>
        </div>
      </div>

      <div>
        {/* Filters */}
        <div className="mb-6 flex items-center justify-between gap-4">
          {/* Search */}
          <div className="relative w-full">
            <Input
              type="text"
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              placeholder="Search users..."
              className="w-full pl-10"
            />
            <Search className="absolute left-3 top-2.5 size-5 text-gray-400" />
          </div>

          {/* Risk Level Filter */}
          <Select onValueChange={handleRiskLevelChange} value={riskLevelFilter}>
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Select Risk Level" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Risk Levels</SelectItem>
              <SelectItem value={RiskLevel.CRITICAL}>Critical</SelectItem>
              <SelectItem value={RiskLevel.HIGH}>High</SelectItem>
              <SelectItem value={RiskLevel.MEDIUM}>Medium</SelectItem>
              <SelectItem value={RiskLevel.LOW}>Low</SelectItem>
            </SelectContent>
          </Select>

          {/* Department Filter */}
          <Select
            onValueChange={handleDepartmentChange}
            value={departmentFilter}
          >
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Select Department" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Departments</SelectItem>
              {departmentList.map(department => (
                <SelectItem key={department} value={department}>
                  {department}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Button variant="outline" onClick={handleReset}>
            <RotateCcw className="size-5" />
            Reset
          </Button>
        </div>

        {/* User Table */}
        <div>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>User</TableHead>
                <TableHead>Department</TableHead>
                <TableHead>Risk Level</TableHead>
                <TableHead className="text-center">Risk Score</TableHead>
                <TableHead>{copy.clicksColumn}</TableHead>
                <TableHead>Compromised</TableHead>
                <TableHead>Reports</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <TableRow key={i}>
                    <TableCell>
                      <Skeleton className="h-4 w-32 rounded bg-gray-200" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-20 rounded bg-gray-200" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="mx-auto h-4 w-16 rounded bg-gray-200" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="ml-auto h-4 w-12 rounded bg-gray-200" />
                    </TableCell>
                  </TableRow>
                ))
              ) : users.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={7}
                    className="text-center text-muted-foreground"
                  >
                    No users found
                  </TableCell>
                </TableRow>
              ) : (
                users.map(user => (
                  <TableRow key={user.userId}>
                    <TableCell>
                      <div className="flex items-center">
                        <div className="flex size-8 items-center justify-center rounded-full bg-gray-200 text-sm font-medium text-gray-600">
                          {user.firstName?.[0]}
                          {user.lastName?.[0]}
                        </div>
                        <div className="ml-3">
                          <div className="font-medium text-foreground">
                            {user.fullName}
                          </div>
                          <div className="text-sm text-muted-foreground">
                            {user.email}
                          </div>
                        </div>
                        {user.isRepeatOffender && (
                          <span className="ml-2 inline-flex items-center rounded bg-red-100 px-2 py-0.5 text-xs font-medium text-red-800">
                            Repeat
                          </span>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>{user.department || '-'}</TableCell>
                    <TableCell>
                      <span
                        className={`inline-flex rounded-full px-2 py-1 text-xs font-medium ${getRiskLevelColor(user.riskLevel)}`}
                      >
                        {getRiskLevelLabel(user.riskLevel)}
                      </span>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <div className="h-2 w-16 overflow-hidden rounded-full bg-white/25">
                          <div
                            className={`h-full rounded-full ${
                              user.riskScore >= 70
                                ? 'bg-red-500'
                                : user.riskScore >= 40
                                  ? 'bg-orange-500'
                                  : user.riskScore >= 20
                                    ? 'bg-yellow-500'
                                    : 'bg-green-500'
                            }`}
                            style={{
                              width: `${Math.min(100, user.riskScore)}%`,
                            }}
                          />
                        </div>
                        <span className="w-12 text-nowrap text-sm font-medium text-foreground ">
                          {user.riskScore.toString()} %
                        </span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <span
                        className={
                          user.emailsClicked > 0
                            ? 'font-medium text-orange-600'
                            : 'text-muted-foreground'
                        }
                      >
                        {user.emailsClicked}
                      </span>
                    </TableCell>
                    <TableCell>
                      <span
                        className={
                          user.dataSubmissions > 0
                            ? 'font-medium text-red-600'
                            : 'text-muted-foreground'
                        }
                      >
                        {user.dataSubmissions}
                      </span>
                    </TableCell>
                    <TableCell>
                      <span
                        className={
                          user.emailsReported > 0
                            ? 'font-medium text-green-600'
                            : 'text-muted-foreground'
                        }
                      >
                        {user.emailsReported}
                      </span>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>

          {/* Pagination */}
          {/* {!showRepeatOffenders && totalPages > 1 && ( */}
          {totalCount > (queryParams.pageSize || 0) && (
            <div className="mt-6 flex justify-end">
              <Pagination
                total={totalCount}
                perPage={queryParams.pageSize || 0}
                currentPage={currentPageIndex}
                onPageChange={handlePageChange}
              />
            </div>
          )}
        </div>
      </div>

      {/* Export Modal */}
      <ExportReportModal
        isOpen={exportModalOpen}
        onClose={() => setExportModalOpen(false)}
        onExport={handleExport}
        title="Export User Risk Report"
        loading={exporting}
      />
    </div>
  );
};

export default UserRiskReport;
