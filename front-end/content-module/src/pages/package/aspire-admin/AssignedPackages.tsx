import { Calendar, Filter, Package, Search } from 'lucide-react';
import { useEffect, useState } from 'react';

import { Badge } from 'common/Badge';
import {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import { Progress } from 'common/Progress';
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
import Border from 'components/UserBorder';
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { IGetListParams, IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString } from 'utils/Helper';

interface PackageAssignment {
  packageName: string;
  productName: string;
  licenseCount: number;
  licenseUsed: number;
  status: 'ACTIVE' | 'EXPIRING_SOON' | 'EXPIRED' | string;
  licenseExpiry: string;
}

interface UserPackageSummary {
  totalAssignedPackages: number;
  packages: PackageAssignment[];
}

const AspireAdminAssignedPackages = () => {
  const { userInfo } = useStore();
  const [loading, setLoading] = useState<boolean>(true);
  const [assignedPackages, setAssignedPackages] = useState<UserPackageSummary>({
    totalAssignedPackages: 0,
    packages: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    clientAdminId: userInfo.userId,
    search: '',
    statusFilter: '',
  });
  const searchDebounce = useDebounce(queryString, 500);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchPackageData();
    }
  }, [searchDebounce]);

  const fetchPackageData = async () => {
    setLoading(true);

    try {
      const response: IResponse<UserPackageSummary> = await apiClient.get(
        API_END_POINTS.ASSIGNED_PACKAGE_LIST + queryString,
      );
      setAssignedPackages(response.data);
    } catch (error) {
      console.error('Error fetching packages:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return (
          <Badge variant="default" className="content-bg-primary">
            Active
          </Badge>
        );
      case 'EXPIRING_SOON':
        return <Badge variant="destructive">Expiring Soon</Badge>;
      case 'EXPIRED':
        return <Badge variant="secondary">Expired</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  return (
    <div className="content-space-y-6 content-text-white">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-tracking-tight content-text-white">
            Package Management
          </h1>
          <p className="content-text-muted-foreground">
            Manage training packages and track performance
          </p>
        </div>
      </div>

      <Border>
        <CardHeader>
          <CardTitle className="content-mb-2 content-flex content-items-center content-gap-2">
            <Package className="content-size-5" />
            Assigned Packages ({assignedPackages?.totalAssignedPackages})
          </CardTitle>
          <CardDescription>
            Packages currently assigned to users in your organization
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="content-mb-6 content-flex content-gap-2">
            <div className="content-relative content-flex-1">
              <Search className="content-absolute content-left-3 content-top-3 content-size-4 content-text-muted-foreground" />
              <Input
                id="search"
                placeholder="Search packages..."
                value={queryParams.search}
                onChange={e =>
                  setQueryParams({ ...queryParams, search: e.target.value })
                }
                className="content-bg-transparent content-pl-9"
              />
            </div>
            <Select
              value={queryParams.statusFilter || 'ALL'}
              onValueChange={e => {
                e === 'ALL'
                  ? setQueryParams(prevState => ({
                      ...prevState,
                      statusFilter: '',
                    }))
                  : setQueryParams(prevState => ({
                      ...prevState,
                      statusFilter: e,
                    }));
              }}
            >
              <SelectTrigger className="!content-w-40 content-bg-transparent">
                <Filter className="content-mr-2 content-size-4" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Status</SelectItem>
                <SelectItem value="ACTIVE">Active</SelectItem>
                <SelectItem value="EXPIRING_SOON">Expiring Soon</SelectItem>
                <SelectItem value="EXPIRED">Expired</SelectItem>
              </SelectContent>
            </Select>
          </div>
          {loading ? (
            <AssignedPackagesLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Package Name</TableHead>
                  <TableHead>Product Name</TableHead>
                  <TableHead>Assigned Users</TableHead>
                  <TableHead>License Used</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>License Expiry</TableHead>
                  {/* <TableHead>Topics</TableHead>
                <TableHead>Certificates</TableHead> */}
                </TableRow>
              </TableHeader>
              <TableBody>
                {assignedPackages?.packages.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} className="content-text-center">
                      Assigned packages not available
                    </TableCell>
                  </TableRow>
                )}
                {assignedPackages?.packages.map(
                  (pkg: PackageAssignment, index: number) => (
                    <TableRow key={index}>
                      <TableCell>
                        <div>
                          <div className="content-font-medium">
                            {pkg.packageName}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>{pkg.productName}</TableCell>
                      <TableCell>{pkg.licenseCount}</TableCell>
                      <TableCell>
                        <div className="content-flex content-items-center content-gap-2">
                          <Progress
                            value={(pkg.licenseUsed / pkg.licenseCount) * 100}
                            className="content-w-16"
                          />
                          <span className="content-text-sm">
                            {pkg.licenseUsed}/{pkg.licenseCount}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell>{getStatusBadge(pkg.status)}</TableCell>
                      <TableCell>
                        <div className="content-flex content-items-center content-gap-1">
                          <Calendar className="content-size-3" />
                          {pkg.licenseExpiry}
                        </div>
                      </TableCell>
                      {/* <TableCell>{pkg.courses}</TableCell>
                    <TableCell>
                      <div className="content-flex content-items-center content-gap-1">
                        <Award className="content-h-3 content-w-3" />
                        {pkg.certificatesIssued}
                      </div>
                    </TableCell> */}
                    </TableRow>
                  ),
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Border>
    </div>
  );
};

export default AspireAdminAssignedPackages;
