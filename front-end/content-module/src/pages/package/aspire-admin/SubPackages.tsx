import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
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
import SearchSelect from 'components/SearchSelect';
import TableLoader from 'components/skeleton/TableLoader';
import AspireAdminAssignSubPackage from 'features/package/sub-package/aspire-assign/AssignSubPackage';
import AspireAdminEditSubPackage from 'features/package/sub-package/aspire-edit-sub-package/EditSubPackage';
import ViewSubPackage from 'features/package/sub-package/View';
import ViewLearners from 'features/package/sub-package/ViewLearners';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import {
  Edit,
  Eye,
  Filter,
  Package2,
  Search,
  Trash2,
  UserPlus,
  Users,
} from 'lucide-react';
import { CourseStatus, IGetListParams, IList, IResponse, Status } from 'models/Global';
import { IProduct } from 'models/Product';
import { ISubPackageDetails } from 'models/SubPackage';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

interface IOrganization {
  id: string;
  organizationName: string;
}

interface IOrganizationsResponse {
  clientAdmins: IOrganization[];
  offset: number;
  pageSize: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

enum modalTypes {
  CreateAndAssignProduct = 'create_and_assign_product',
  AssignSubPackage = 'assign_sub_package',
  ViewSubPackage = 'view_sub_package',
  EditSubPackage = 'edit_sub_package',
  ViewLearners = 'view_learners',
  None = 'none',
}

const AspireAdminSubPackages = () => {
  const [loading, setLoading] = useState(true);
  const [isOpenModal, setIsOpenModal] = useState<modalTypes>(modalTypes.None);
  const [subPackages, setSubPackages] = useState<IList<any>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 10,
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    status: CourseStatus.ALL,
    productId: '',
    clientAdminId: '',
  });
  const [products, setProducts] = useState<IList<IProduct>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 1000,
  });
  const [organizations, setOrganizations] = useState<IOrganizationsResponse>({
    clientAdmins: [],
    total: 0,
    offset: 0,
    pageSize: 1000,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const apiClient = useAPI();
  const [selectedSubPackage, setSelectedSubPackage] =
    useState<ISubPackageDetails>();
  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchSubPackages();
    }
  }, [searchDebounce]);

  const fetchSubPackages = async () => {
    try {
      setLoading(true);
      const response: IResponse<IList<any>> = await apiClient.get(
        API_END_POINTS.SUB_PACKAGE_LIST + queryString,
      );
      setSubPackages(response.data);
    } catch (error) {
      console.error('Error fetching sub-packages:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchOrganizations = async () => {
    try {
      const response: IResponse<IOrganizationsResponse> = await apiClient.get(
        API_END_POINTS.ORGANIZATION_LIST + 'pageSize=1000&status=ACTIVE',
      );
      setOrganizations(response.data);
    } catch (error) {
      console.error('Error fetching organizations:', error);
    }
  };

  const fetchProducts = async () => {
    try {
      const response: IResponse<IList<IProduct>> = await apiClient.get(
        API_END_POINTS.PRODUCT_LIST + 'pageSize=1000',
      );
      setProducts(response.data);
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  };

  useEffect(() => {
    fetchProducts();
    fetchOrganizations();
  }, []);

  const handleView = (subPackage: ISubPackageDetails) => {
    setIsOpenModal(modalTypes.ViewSubPackage);
    setSelectedSubPackage(subPackage);
  };

  const handleEdit = (subPackage: ISubPackageDetails) => {
    setIsOpenModal(modalTypes.EditSubPackage);
    setSelectedSubPackage(subPackage);
  };

  // const handleDelete = (id: string, name: string) => {};

  const handleAssignProduct = (subPackage: any) => {
    setIsOpenModal(modalTypes.AssignSubPackage);
    setSelectedSubPackage(subPackage);
  };

  const handleViewLearners = (subPackage: any) => {
    setIsOpenModal(modalTypes.ViewLearners);
    setSelectedSubPackage(subPackage);
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    setQueryParams({
      ...InitGetListParams,
      search: '',
      status: CourseStatus.ALL,
      productId: '',
      clientAdminId: '',
    });
  };
  return (
    <div className="content-space-y-6">
      {/* Header */}
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-text-white dark:content-text-white">
            Sub Packages Aspire Admin
          </h1>
          <p className="content-mt-2 content-text-gray-600 dark:content-text-gray-400">
            Manage and organize training content packages for your organization
          </p>
        </div>

        {/* <Button
          onClick={() => setIsOpenModal(modalTypes.CreateAndAssignProduct)}
        >
          <Plus className="content-h-4 content-w-4" />
          Create Sub Package
        </Button> */}
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-gap-2">
            <Filter className="content-size-5" />
            Filters & Search
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="content-grid content-grid-cols-6 content-gap-4">
            <div className="content-col-span-2">
              <div className="content-relative">
                <Search className="content-absolute content-left-3 content-top-1/2 content-size-4 -content-translate-y-1/2 content-transform content-text-gray-400" />
                <Input
                  placeholder="Search sub-packages by name..."
                  value={queryParams.search}
                  onChange={e => {
                    setQueryParams({ ...queryParams, search: e.target.value });
                  }}
                  className="content-pl-10"
                />
              </div>
            </div>
            <Select
              value={queryParams.status}
              onValueChange={value => {
                setQueryParams({
                  ...queryParams,
                  status:
                    value === 'all'
                      ? CourseStatus.ALL
                      : (value as CourseStatus),
                });
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder="Filter by Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value={CourseStatus.ACTIVE}>Active</SelectItem>
                <SelectItem value={CourseStatus.INACTIVE}>Inactive</SelectItem>
                <SelectItem value={CourseStatus.EXPIRED}>Expired</SelectItem>
              </SelectContent>
            </Select>
            <SearchSelect
              items={products.items.map(product => ({
                value: product.productId,
                label: product.productName,
              }))}
              value={queryParams.productId}
              onValueChange={value =>
                setQueryParams({ ...queryParams, productId: value })
              }
              placeholder="Filter by Product"
            />
            <SearchSelect
              items={organizations.clientAdmins.map(organization => ({
                value: organization.id,
                label: organization.organizationName,
              }))}
              value={queryParams.clientAdminId}
              onValueChange={value =>
                setQueryParams({ ...queryParams, clientAdminId: value })
              }
              placeholder="Filter by Organization"
            />
            <Button
              size="sm"
              variant="outline"
              onClick={handleReset}
              title="Reset Filters"
            >
              Reset Filters
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Sub-Packages Table */}
      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-gap-2">
            <Package2 className="content-size-5" />
            Total Sub Packages ({subPackages.total})
          </CardTitle>
          <CardDescription>
            Manage your training sub-packages and assign them to users
          </CardDescription>
        </CardHeader>
        <CardContent className="content-p-0">
          <div>
            {loading ? (
              <TableLoader />
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>SL</TableHead>
                    <TableHead>Sub-Package Name</TableHead>
                    <TableHead>Product</TableHead>
                    {/* <TableHead>Company</TableHead> */}
                    <TableHead>Topics</TableHead>
                    {/* <TableHead>Created By</TableHead>
                  <TableHead>Created On</TableHead> */}
                    <TableHead>Status</TableHead>
                    {/* <TableHead>Last Updated</TableHead> */}
                    <TableHead className="content-text-center">
                      Actions
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {subPackages.items.map((pkg, index) => (
                    <TableRow key={pkg.id}>
                      <TableCell>{index + 1}</TableCell>
                      <TableCell>{pkg.name}</TableCell>
                      <TableCell>
                        {pkg.productName}
                      </TableCell>
                      {/* <TableCell className="content-text-gray-700 dark:content-text-gray-300">
                      {pkg.companyName}
                    </TableCell> */}
                      <TableCell>
                        <Badge variant="secondary">
                          {pkg.topicId.length} topics
                        </Badge>
                      </TableCell>
                      {/* <TableCell>{pkg.createdBy}</TableCell>
                    <TableCell>{pkg.createdOn}</TableCell> */}
                      <TableCell>
                        <Badge
                          variant={
                            pkg.status === Status.ACTIVE
                              ? 'default'
                              : pkg.status === Status.INACTIVE
                                ? 'destructive'
                                : 'secondary'
                          }
                        >
                          {pkg.status}
                        </Badge>
                      </TableCell>
                      {/* <TableCell className="content-text-gray-700 dark:content-text-gray-300">
                      {pkg.lastUpdated}
                    </TableCell> */}
                      <TableCell className="content-text-right">
                        <div className="content-flex content-items-center content-justify-center content-gap-2">
                          <span title={
                            pkg.status === Status.INACTIVE
                              ? "This sub-package is inactive. Activate it before assigning it to users."
                              : "Assign this sub-package to users."
                          }>

                            <Button
                              size="sm"
                              onClick={() => handleAssignProduct(pkg)}
                              disabled={pkg.status === Status.INACTIVE}
                            >
                              <UserPlus className="content-size-3" />
                              Assign Users
                            </Button>
                          </span>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleViewLearners(pkg)}
                            disabled={!pkg.assignedUserCount}
                            title="View assigned learners"
                          >
                            <Users className="content-size-3" />
                            View Learners
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleView(pkg)}
                            title="View Details"
                          >
                            <Eye className="content-size-3" />
                          </Button>
                          <span
                            title={
                              pkg.isAlreadyAssigned
                                ? "This package is already assigned, so it can't be edited."
                                : 'Edit Sub-Package'
                            }
                            className="content-inline-block"
                          >
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => handleEdit(pkg)}
                              disabled={pkg.isAlreadyAssigned === true}
                            >
                              <Edit className="content-size-3" />
                            </Button>
                          </span>
                          <span
                            title={
                              pkg.isAlreadyAssigned
                                ? "This package is already assigned, so it can't be deleted."
                                : 'Delete Sub-Package'
                            }
                            className="content-inline-block"
                          >
                            <Button
                              size="sm"
                              variant="outline"
                              // onClick={() => handleDelete(pkg.id, pkg.name)}
                              disabled={pkg.isAlreadyAssigned === true}
                            >
                              <Trash2 className="content-size-3" />
                            </Button>
                          </span>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}

            <div className="content-flex content-justify-end content-pt-4">
              <Pagination
                total={subPackages.total}
                perPage={subPackages.pageSize}
                onPageChange={onPageChangeHandler}
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* {isOpenModal === modalTypes.CreateAndAssignProduct && (
        <CreateAndAssignProduct
          hostPath={routes}
          product={{} as IAssignedLicense}
          isOpen={isOpenModal === modalTypes.CreateAndAssignProduct}
          onClose={() => setIsOpenModal(modalTypes.None)}
        />
      )} */}

      {isOpenModal === modalTypes.AssignSubPackage && (
        <AspireAdminAssignSubPackage
          selectedSubPackage={selectedSubPackage as ISubPackageDetails}
          refetch={() => { }}
          isOpen={isOpenModal === modalTypes.AssignSubPackage}
          onClose={() => setIsOpenModal(modalTypes.None)}
        />
      )}
      {isOpenModal === modalTypes.ViewSubPackage && (
        <ViewSubPackage
          selectedSubPackage={selectedSubPackage as ISubPackageDetails}
          isOpen={isOpenModal === modalTypes.ViewSubPackage}
          onClose={() => setIsOpenModal(modalTypes.None)}
        />
      )}
      {isOpenModal === modalTypes.EditSubPackage && (
        <AspireAdminEditSubPackage
          selectedSubPackage={selectedSubPackage as ISubPackageDetails}
          isOpen={isOpenModal === modalTypes.EditSubPackage}
          onClose={() => setIsOpenModal(modalTypes.None)}
          refetch={fetchSubPackages}
        />
      )}
      {isOpenModal === modalTypes.ViewLearners && (
        <ViewLearners
          isOpen={isOpenModal === modalTypes.ViewLearners}
          onClose={() => setIsOpenModal(modalTypes.None)}
          selectedSubPackage={selectedSubPackage as ISubPackageDetails}
        />
      )}
    </div>
  );
};

export default AspireAdminSubPackages;
