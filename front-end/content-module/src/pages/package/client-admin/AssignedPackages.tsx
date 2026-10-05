import {
  CalendarClock,
  CreditCard,
  Layers,
  Package,
  PieChart,
  Users,
} from 'lucide-react';
import { ReactNode, useEffect, useState } from 'react';

import { Badge } from 'common/Badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import { Progress } from 'common/Progress';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import Border from 'components/UserBorder';
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { IAssignedLicense, ILicenseOverviewSummary } from 'models/License';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { HumanizeDate, objectToQueryString } from 'utils/Helper';

const EMPTY_OVERVIEW_SUMMARY: ILicenseOverviewSummary = {
  totalLicenses: 0,
  assignedUsers: 0,
  assignedUsersPercent: 0,
  availableSeats: 0,
  availableSeatsPercent: 0,
  utilizationRate: 0,
  utilizationChangeVsLastMonth: 0,
  expiringSoon: 0,
  expiringSoonDays: 30,
  activeProducts: 0,
};

const PACKAGE_ICON_COLORS = [
  'content-bg-[#8B5CF6]',
  'content-bg-[#F59E0B]',
  'content-bg-[#14B8A6]',
  'content-bg-[#22C55E]',
  'content-bg-[#3B82F6]',
  'content-bg-[#EC4899]',
];

const toPercent = (value: number): string => `${value.toFixed(1)}%`;

const formatChangeVsLastMonth = (value: number): string => {
  const sign = value > 0 ? '+' : '';
  return `${sign}${value.toFixed(1)}% vs last month`;
};

const getPackageIconColor = (name: string): string => {
  let hash = 0;
  for (let i = 0; i < name.length; i += 1) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return PACKAGE_ICON_COLORS[Math.abs(hash) % PACKAGE_ICON_COLORS.length];
};

const getUtilization = (pkg: IAssignedLicense): number => {
  if (!pkg.licenseCount) return 0;
  return (pkg.usedLicenseCount / pkg.licenseCount) * 100;
};

const OverviewStatCard = ({
  icon,
  iconClassName,
  label,
  value,
  subtitle,
}: {
  icon: ReactNode;
  iconClassName: string;
  label: string;
  value: string;
  subtitle: string;
}) => (
  <Card>
    <div className="content-flex content-items-start content-gap-3 content-p-4">
      <div
        className={`content-flex content-size-10 content-shrink-0 content-items-center content-justify-center content-rounded-lg ${iconClassName}`}
      >
        {icon}
      </div>
      <div className="content-min-w-0">
        <p className="content-text-sm content-text-muted-foreground">{label}</p>
        <p className="content-text-2xl content-font-bold content-text-white">
          {value}
        </p>
        <p className="content-text-xs content-text-muted-foreground">
          {subtitle}
        </p>
      </div>
    </div>
  </Card>
);

const ClientAdminAssignedPackages = () => {
  const { userInfo } = useStore();
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(true);
  const [overviewSummary, setOverviewSummary] =
    useState<ILicenseOverviewSummary>(EMPTY_OVERVIEW_SUMMARY);
  const [assignedPackages, setAssignedPackages] = useState<
    IList<IAssignedLicense>
  >({
    items: [],
    total: 0,
    offset: 0,
    pageSize: InitGetListParams.pageSize,
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
  });
  const searchDebounce = useDebounce(queryString, 500);

  useEffect(() => {
    setQueryString(objectToQueryString(queryParams));
  }, [queryParams]);

  useEffect(() => {
    const fetchOverviewSummary = async () => {
      try {
        const response: IResponse<ILicenseOverviewSummary> =
          await apiClient.get(
            API_END_POINTS.CLIENT_ADMIN_LICENSE_OVERVIEW_SUMMARY,
          );
        setOverviewSummary(response.data ?? EMPTY_OVERVIEW_SUMMARY);
      } catch (error) {
        console.error('Error fetching license overview summary:', error);
      }
    };

    fetchOverviewSummary();
  }, [apiClient]);

  useEffect(() => {
    if (!searchDebounce || !userInfo?.userId) return;

    const fetchPackageData = async () => {
      setLoading(true);

      try {
        const response: IResponse<IList<IAssignedLicense>> =
          await apiClient.get(
            API_END_POINTS.CLIENT_ADMIN_ASSIGN_PRODUCT_LIST.replace(
              ':clientAdminId',
              userInfo.userId,
            ) + searchDebounce,
          );
        setAssignedPackages(response.data);
      } catch (error) {
        console.error('Error fetching packages:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchPackageData();
  }, [searchDebounce, apiClient, userInfo?.userId]);

  const getStatusBadge = (status: string) => {
    const normalized = status?.toUpperCase().replace(/\s+/g, '_') || '';

    switch (normalized) {
      case 'ACTIVE':
        return <Badge variant="default">Active</Badge>;
      case 'WARNING':
      case 'EXPIRING_SOON':
        return (
          <Badge
            variant="outline"
            className="content-border-amber-500 content-bg-amber-500/20 content-text-amber-400"
          >
            Expiring Soon
          </Badge>
        );
      case 'EXPIRED':
        return <Badge variant="destructive">Expired</Badge>;
      default:
        return (
          <Badge
            variant="outline"
            className="content-border-transparent content-text-white"
          >
            {status || '-'}
          </Badge>
        );
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const packageItems = assignedPackages?.items ?? [];
  const pageSize =
    assignedPackages?.pageSize ||
    queryParams.pageSize ||
    InitGetListParams.pageSize;
  const currentOffset = queryParams.offset ?? 0;
  const showingFrom =
    packageItems.length === 0 ? 0 : currentOffset * pageSize + 1;
  const showingTo =
    packageItems.length === 0
      ? 0
      : currentOffset * pageSize + packageItems.length;
  const showingTotal = assignedPackages?.total || 0;

  return (
    <div className="content-space-y-6 content-text-white">
      <div>
        <h1 className="content-text-3xl content-font-bold content-tracking-tight content-text-white">
          License History
        </h1>
        <p className="content-text-muted-foreground">
          {
            "Monitor your organization's license usage, availability, and expiration status."
          }
        </p>
      </div>

      <div className="content-grid content-grid-cols-1 content-gap-4 sm:content-grid-cols-2 lg:content-grid-cols-3 xl:content-grid-cols-6">
        <OverviewStatCard
          icon={<Package className="content-size-5" />}
          iconClassName="content-bg-[#8B5CF6]/20 content-text-[#A78BFA]"
          label="Total Licenses"
          value={overviewSummary.totalLicenses.toLocaleString()}
          subtitle="All purchased licenses"
        />
        <OverviewStatCard
          icon={<Users className="content-size-5" />}
          iconClassName="content-bg-[#14B8A6]/20 content-text-[#2DD4BF]"
          label="Assigned Users"
          value={overviewSummary.assignedUsers.toLocaleString()}
          subtitle={`${toPercent(overviewSummary.assignedUsersPercent)} of total licenses`}
        />
        <OverviewStatCard
          icon={<CreditCard className="content-size-5" />}
          iconClassName="content-bg-[#3B82F6]/20 content-text-[#60A5FA]"
          label="Available Seats"
          value={overviewSummary.availableSeats.toLocaleString()}
          subtitle={`${toPercent(overviewSummary.availableSeatsPercent)} of total licenses`}
        />
        <OverviewStatCard
          icon={<PieChart className="content-size-5" />}
          iconClassName="content-bg-[#F59E0B]/20 content-text-[#FBBF24]"
          label="Utilization Rate"
          value={toPercent(overviewSummary.utilizationRate)}
          subtitle={formatChangeVsLastMonth(
            overviewSummary.utilizationChangeVsLastMonth,
          )}
        />
        <OverviewStatCard
          icon={<CalendarClock className="content-size-5" />}
          iconClassName="content-bg-[#EF4444]/20 content-text-[#F87171]"
          label="Expiring Soon"
          value={overviewSummary.expiringSoon.toLocaleString()}
          subtitle={`Within next ${overviewSummary.expiringSoonDays} days`}
        />
        <OverviewStatCard
          icon={<Layers className="content-size-5" />}
          iconClassName="content-bg-[#06B6D4]/20 content-text-[#22D3EE]"
          label="Active Products"
          value={overviewSummary.activeProducts.toLocaleString()}
          subtitle="With active licenses"
        />
      </div>

      <Border>
        <CardHeader>
          <CardTitle className="content-mb-2 content-flex content-items-center content-gap-2">
            <Package className="content-size-5" />
            License Summary by Product / Package
          </CardTitle>
          <CardDescription>
            Packages currently assigned to users in your organization
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="content-mb-6 content-flex content-gap-2">
            <div className="content-relative content-flex-1">
              <Input
                id="search"
                placeholder="Search packages..."
                value={queryParams.search}
                onChange={e =>
                  setQueryParams(prev => ({
                    ...prev,
                    search: e.target.value,
                    offset: 0,
                  }))
                }
                className="content-bg-transparent"
              />
            </div>
          </div>
          {loading ? (
            <AssignedPackagesLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Package Name</TableHead>
                  <TableHead>Product Name</TableHead>
                  <TableHead>Total Seats</TableHead>
                  <TableHead>Assigned Users</TableHead>
                  <TableHead>Available Seats</TableHead>
                  <TableHead>Utilization %</TableHead>
                  <TableHead>Expiry Date</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {packageItems.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={8} className="content-text-center">
                      Assigned packages not available
                    </TableCell>
                  </TableRow>
                )}
                {packageItems.map((pkg: IAssignedLicense) => {
                  const packageName = pkg.packageDetails?.packageName || '-';
                  const utilization = getUtilization(pkg);
                  const availableSeats = Math.max(
                    (pkg.licenseCount || 0) - (pkg.usedLicenseCount || 0),
                    0,
                  );

                  return (
                    <TableRow key={pkg.id}>
                      <TableCell>
                        <div className="content-flex content-items-center content-gap-2">
                          <span
                            className={`content-flex content-size-7 content-shrink-0 content-items-center content-justify-center content-rounded content-text-xs content-font-semibold content-text-white ${getPackageIconColor(packageName)}`}
                          >
                            {packageName.charAt(0).toUpperCase()}
                          </span>
                          <span className="content-font-medium">
                            {packageName}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell>{pkg.product?.productName || '-'}</TableCell>
                      <TableCell>{pkg.licenseCount ?? 0}</TableCell>
                      <TableCell>{pkg.usedLicenseCount ?? 0}</TableCell>
                      <TableCell>{availableSeats}</TableCell>
                      <TableCell>
                        <div className="content-flex content-items-center content-gap-2">
                          <Progress
                            value={utilization}
                            className="content-w-16"
                          />
                          <span className="content-text-sm">
                            {toPercent(utilization)}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell>
                        {pkg.expiryDate ? HumanizeDate(pkg.expiryDate) : '-'}
                      </TableCell>
                      <TableCell>
                        {getStatusBadge(
                          pkg.licenseStatus ||
                            pkg.packageDetails?.packageStatus ||
                            '',
                        )}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          )}
          <div className="content-mt-4 content-flex content-flex-wrap content-items-center content-justify-between content-gap-3">
            <p className="content-text-sm content-text-muted-foreground">
              Showing {showingFrom} to {showingTo} of {showingTotal} packages
            </p>
            <Pagination
              total={showingTotal}
              perPage={pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Border>
    </div>
  );
};

export default ClientAdminAssignedPackages;
