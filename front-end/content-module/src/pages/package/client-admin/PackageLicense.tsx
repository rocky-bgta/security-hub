import { Search } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
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
import Border from 'components/UserBorder';
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { IDropdownOption } from 'models/DropDown';
import { CourseStatus, IGetListParams, IList, IResponse } from 'models/Global';
import { IAssignedLicense, ILicenseAssignment } from 'models/License';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  HumanizeDate,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

const PACKAGE_ICON_COLORS = [
  'content-bg-[#8B5CF6]',
  'content-bg-[#F59E0B]',
  'content-bg-[#14B8A6]',
  'content-bg-[#22C55E]',
  'content-bg-[#3B82F6]',
  'content-bg-[#EC4899]',
];

const DEFAULT_ASSIGNMENT_PARAMS: IGetListParams = {
  ...InitGetListParams,
  search: '',
  packageId: '',
  productId: '',
  department: '',
  status: CourseStatus.ALL,
};

const getPackageIconColor = (name: string): string => {
  let hash = 0;
  for (let i = 0; i < name.length; i += 1) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return PACKAGE_ICON_COLORS[Math.abs(hash) % PACKAGE_ICON_COLORS.length];
};

const getAssignmentKey = (item: ILicenseAssignment, index: number): string =>
  `${item.userId}-${item.subPackageId}-${item.packageId}-${item.productId}-${index}`;

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
    case 'EXPIRING':
      return (
        <Badge
          variant="outline"
          className="content-border-amber-500 content-bg-amber-500/20 content-text-amber-400"
        >
          Expiring
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

const ClientAdminPackageLicense = () => {
  const { userInfo } = useStore();
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(true);
  const [licenseData, setLicenseData] = useState<IList<ILicenseAssignment>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: InitGetListParams.pageSize,
  });
  const [assignedLicenses, setAssignedLicenses] = useState<IAssignedLicense[]>(
    [],
  );
  const [departments, setDepartments] = useState<IDropdownOption[]>([]);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...DEFAULT_ASSIGNMENT_PARAMS,
  });
  const searchDebounce = useDebounce(queryString, 500);

  useEffect(() => {
    setQueryString(objectToQueryString(queryParams));
  }, [queryParams]);

  useEffect(() => {
    const fetchFilterOptions = async () => {
      if (!userInfo?.userId) return;

      try {
        const [assignedResponse, departmentResponse] = await Promise.all([
          apiClient.get(
            API_END_POINTS.CLIENT_ADMIN_ASSIGN_PRODUCT_LIST.replace(
              ':clientAdminId',
              userInfo.userId,
            ) + 'offset=0&pageSize=100',
          ) as Promise<IResponse<IList<IAssignedLicense>>>,
          apiClient.get(
            `${API_END_POINTS.GET_DEPARTMENT_LIST}clientAdminId=${userInfo.userId}&active=true&isSystemDefined=true&pageSize=1000`,
          ),
        ]);

        setAssignedLicenses(assignedResponse.data?.items ?? []);

        if (isSuccessResponse(departmentResponse.statusCode)) {
          setDepartments(
            (departmentResponse.data?.items ?? []).map(
              (department: IDropdownOption) => ({
                id: department.name,
                name: department.name,
              }),
            ),
          );
        }
      } catch (error) {
        console.error('Error fetching assignment filters:', error);
      }
    };

    fetchFilterOptions();
  }, [apiClient, userInfo?.userId]);

  useEffect(() => {
    if (!searchDebounce) return;

    const fetchLicenseAssignments = async () => {
      setLoading(true);
      try {
        const response: IResponse<IList<ILicenseAssignment>> =
          await apiClient.get(
            API_END_POINTS.CLIENT_LICENSE_ASSIGNMENTS + searchDebounce,
          );
        setLicenseData(response.data);
      } catch (error) {
        console.error('Error fetching license assignments:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchLicenseAssignments();
  }, [searchDebounce, apiClient]);

  const uniquePackages = useMemo(
    () =>
      assignedLicenses.filter(
        (item, index, self) =>
          item.packageId &&
          index === self.findIndex(pkg => pkg.packageId === item.packageId),
      ),
    [assignedLicenses],
  );

  const uniqueProducts = useMemo(
    () =>
      assignedLicenses.filter(
        (item, index, self) =>
          item.productId &&
          index ===
            self.findIndex(product => product.productId === item.productId),
      ),
    [assignedLicenses],
  );

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleFilterChange = (updates: Partial<IGetListParams>) => {
    setQueryParams(prev => ({
      ...prev,
      ...updates,
      offset: 0,
    }));
  };

  const handleReset = () => {
    setQueryParams({ ...DEFAULT_ASSIGNMENT_PARAMS });
  };

  const assignmentItems = licenseData?.items ?? [];
  const pageSize =
    licenseData?.pageSize || queryParams.pageSize || InitGetListParams.pageSize;
  const currentOffset = queryParams.offset ?? 0;
  const showingFrom =
    assignmentItems.length === 0 ? 0 : currentOffset * pageSize + 1;
  const showingTo =
    assignmentItems.length === 0
      ? 0
      : currentOffset * pageSize + assignmentItems.length;
  const showingTotal = licenseData?.total || 0;

  return (
    <div className="content-space-y-6 content-text-white">
      <div>
        <h1 className="content-text-3xl content-font-bold content-tracking-tight content-text-white">
          License Assignments
        </h1>
        <p className="content-text-muted-foreground">
          View and manage user license assignments across your organization
        </p>
      </div>

      <Border>
        <CardHeader>
          <div className="content-flex content-flex-col content-gap-4">
            <div>
              <CardTitle className="content-mb-2">Assigned Users</CardTitle>
              <CardDescription>
                Users assigned to packages, products, and sub-packages
              </CardDescription>
            </div>
            <div className="content-grid content-grid-cols-1 content-items-end content-gap-2 md:content-grid-cols-2 xl:content-grid-cols-6">
              <div className="content-flex content-flex-col content-gap-2">
                <Label htmlFor="search">Search</Label>
                <div className="content-relative">
                  <Search className="content-absolute content-left-3 content-top-1/2 content-size-4 -content-translate-y-1/2 content-text-muted-foreground" />
                  <Input
                    id="search"
                    placeholder="Search by user name or email..."
                    value={queryParams.search}
                    onChange={e =>
                      handleFilterChange({ search: e.target.value })
                    }
                    className="content-bg-transparent content-pl-9"
                  />
                </div>
              </div>
              <div className="content-flex content-flex-col content-gap-2">
                <Label htmlFor="packageId">Package</Label>
                <Select
                  value={queryParams.packageId || 'all'}
                  onValueChange={value =>
                    handleFilterChange({
                      packageId: value === 'all' ? '' : value,
                    })
                  }
                >
                  <SelectTrigger id="packageId" className="content-w-full">
                    <SelectValue placeholder="All Packages" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Packages</SelectItem>
                    {uniquePackages.map(pkg => (
                      <SelectItem key={pkg.packageId} value={pkg.packageId}>
                        {pkg.packageDetails?.packageName || pkg.packageId}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="content-flex content-flex-col content-gap-2">
                <Label htmlFor="productId">Product</Label>
                <Select
                  value={queryParams.productId || 'all'}
                  onValueChange={value =>
                    handleFilterChange({
                      productId: value === 'all' ? '' : value,
                    })
                  }
                >
                  <SelectTrigger id="productId" className="content-w-full">
                    <SelectValue placeholder="All Products" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Products</SelectItem>
                    {uniqueProducts.map(product => (
                      <SelectItem
                        key={product.productId}
                        value={product.productId}
                      >
                        {product.product?.productName || product.productId}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="content-flex content-flex-col content-gap-2">
                <Label htmlFor="department">Department</Label>
                <Select
                  value={queryParams.department || 'all'}
                  onValueChange={value =>
                    handleFilterChange({
                      department: value === 'all' ? '' : value,
                    })
                  }
                >
                  <SelectTrigger id="department" className="content-w-full">
                    <SelectValue placeholder="All Departments" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Departments</SelectItem>
                    {departments.map(department => (
                      <SelectItem key={department.id} value={department.id}>
                        {department.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="content-flex content-flex-col content-gap-2">
                <Label htmlFor="status">Status</Label>
                <Select
                  value={queryParams.status || 'ALL'}
                  onValueChange={value =>
                    handleFilterChange({
                      status:
                        value === 'ALL'
                          ? CourseStatus.ALL
                          : (value as CourseStatus),
                    })
                  }
                >
                  <SelectTrigger id="status" className="content-w-full">
                    <SelectValue placeholder="All Status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ALL">All Status</SelectItem>
                    <SelectItem value="ACTIVE">Active</SelectItem>
                    <SelectItem value="EXPIRING_SOON">Expiring Soon</SelectItem>
                    <SelectItem value="EXPIRED">Expired</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <Button variant="outline" onClick={handleReset}>
                Reset
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <AssignedPackagesLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>User</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Department</TableHead>
                  <TableHead>Package Name</TableHead>
                  <TableHead>Product Name</TableHead>
                  <TableHead>Sub Package</TableHead>
                  <TableHead>Assigned Date</TableHead>
                  <TableHead>Expiry Date</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {assignmentItems.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={9} className="content-text-center">
                      License assignments not available
                    </TableCell>
                  </TableRow>
                )}
                {assignmentItems.map((item, index) => (
                  <TableRow key={getAssignmentKey(item, index)}>
                    <TableCell className="content-font-medium">
                      {item.fullName || '-'}
                    </TableCell>
                    <TableCell>{item.email || '-'}</TableCell>
                    <TableCell>{item.department || '-'}</TableCell>
                    <TableCell>
                      <div className="content-flex content-items-center content-gap-2">
                        <span
                          className={`content-flex content-size-7 content-shrink-0 content-items-center content-justify-center content-rounded content-text-xs content-font-semibold content-text-white ${getPackageIconColor(item.packageName || '')}`}
                        >
                          {(item.packageName || '-').charAt(0).toUpperCase()}
                        </span>
                        {item.packageName || '-'}
                      </div>
                    </TableCell>
                    <TableCell>{item.productName || '-'}</TableCell>
                    <TableCell>{item.subPackageName || '-'}</TableCell>
                    <TableCell>
                      {item.assignedDate
                        ? HumanizeDate(item.assignedDate)
                        : '-'}
                    </TableCell>
                    <TableCell>
                      {item.expiryDate ? HumanizeDate(item.expiryDate) : '-'}
                    </TableCell>
                    <TableCell>{getStatusBadge(item.status)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <div className="content-mt-4 content-flex content-flex-wrap content-items-center content-justify-between content-gap-3">
            <p className="content-text-sm content-text-muted-foreground">
              Showing {showingFrom} to {showingTo} of {showingTotal} assignments
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

export default ClientAdminPackageLicense;
