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
import IconBackButton from 'components/IconBackButton';
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import AssignSubPackage from 'features/package/sub-package/assign/AssignSubPackage';
import CreateSubPackage from 'features/package/sub-package/create-sub-package-from-list/CreateSubPackage';
import EditSubPackage from 'features/package/sub-package/edit-sub-package/EditSubPackage';
import ViewSubPackage from 'features/package/sub-package/View';
import ViewLearners from 'features/package/sub-package/ViewLearners';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import {
  Edit,
  Eye,
  Filter,
  Package2,
  Plus,
  Search,
  UserPlus,
  Users,
} from 'lucide-react';
import {
  CourseStatus,
  IGetListParams,
  IList,
  IResponse,
  Status,
} from 'models/Global';
import { IAssignedLicense } from 'models/License';
import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';
import { formatDateAndTime, objectToQueryString } from 'utils/Helper';

enum modalTypes {
  AssignSubPackage = 'assign_sub_package',
  ViewSubPackage = 'view_sub_package',
  EditSubPackage = 'edit_sub_package',
  CreateSubPackage = 'create_sub_package',
  ViewLearners = 'view_learners',
  None = 'none',
}

const SECURITY_TAG = 'Security';
const PRODUCT_FILTER_TAGS = ['Security', 'Phishing', 'Vishing', 'Smishing'];

const hasProductFilterTag = (product: IAssignedLicense) =>
  product.product?.tags?.some(tag => PRODUCT_FILTER_TAGS.includes(tag)) ??
  false;

const getUniqueProducts = (items: IAssignedLicense[]) =>
  items
    .filter(hasProductFilterTag)
    .filter(
      (product, index, self) =>
        product.product &&
        index === self.findIndex(p => p.productId === product.productId),
    );

const getDefaultProductSelection = (uniqueProducts: IAssignedLicense[]) => {
  const securityProduct = uniqueProducts.find(product =>
    product.product?.tags?.includes(SECURITY_TAG),
  );

  if (securityProduct) {
    return {
      productId: securityProduct.productId,
      productFilter: securityProduct.productId,
    };
  }

  if (uniqueProducts.length === 1) {
    return {
      productId: uniqueProducts[0].productId,
      productFilter: uniqueProducts[0].productId,
    };
  }

  return { productId: '', productFilter: 'all' };
};

const ClientAdminSubPackages = () => {
  const { userInfo } = useStore();
  const [searchParams] = useSearchParams();
  const productIdFromUrl = searchParams.get('productId') || '';
  const fromProductPage = searchParams.get('from') === 'product';

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [productFilter, setProductFilter] = useState(productIdFromUrl || 'all');
  const [isOpenModal, setIsOpenModal] = useState<modalTypes>(modalTypes.None);
  const [selectedSubPackage, setSelectedSubPackage] = useState<any>(null);
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
    productId: productIdFromUrl,
    clientAdminId: userInfo?.userId,
  });
  const [products, setProducts] = useState<IList<IAssignedLicense>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 10,
  });
  const [loading, setLoading] = useState(true);
  const searchDebounce = useDebounce(queryString, 1000);
  const apiClient = useAPI();

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
    setLoading(true);
    const response: IResponse<IList<any>> = await apiClient.get(
      API_END_POINTS.SUB_PACKAGE_LIST + queryString,
    );
    setSubPackages(response.data);
    setLoading(false);
  };

  const fetchProducts = async () => {
    if (!userInfo?.userId) return;
    const response: IResponse<IList<IAssignedLicense>> = await apiClient.get(
      API_END_POINTS.CLIENT_ADMIN_ASSIGN_PRODUCT_LIST.replace(
        ':clientAdminId',
        userInfo?.userId,
      ) + 'offset=0&pageSize=100',
    );
    setProducts(response.data);
  };

  const uniqueProducts = useMemo(
    () => getUniqueProducts(products.items),
    [products.items],
  );

  useEffect(() => {
    fetchProducts();
  }, [userInfo]);

  useEffect(() => {
    if (uniqueProducts.length === 0) return;

    if (
      productIdFromUrl &&
      uniqueProducts.some(product => product.productId === productIdFromUrl)
    ) {
      setProductFilter(productIdFromUrl);
      setQueryParams(prev =>
        prev.productId === productIdFromUrl
          ? prev
          : { ...prev, productId: productIdFromUrl },
      );
      return;
    }

    const { productId, productFilter: defaultProductFilter } =
      getDefaultProductSelection(uniqueProducts);
    setProductFilter(defaultProductFilter);
    setQueryParams(prev =>
      prev.productId === productId ? prev : { ...prev, productId },
    );
  }, [uniqueProducts, productIdFromUrl]);

  const handleView = (pkg: any) => {
    setIsOpenModal(modalTypes.ViewSubPackage);
    setSelectedSubPackage(pkg);
  };

  const handleEdit = (pkg: any) => {
    setIsOpenModal(modalTypes.EditSubPackage);
    setSelectedSubPackage(pkg);
  };

  const handleAssignProduct = (pkg: any) => {
    setIsOpenModal(modalTypes.AssignSubPackage);
    setSelectedSubPackage(pkg);
  };

  const handleViewLearners = (pkg: any) => {
    setIsOpenModal(modalTypes.ViewLearners);
    setSelectedSubPackage(pkg);
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    const { productId, productFilter: defaultProductFilter } =
      getDefaultProductSelection(uniqueProducts);
    setQueryParams({
      ...InitGetListParams,
      status: CourseStatus.ALL,
      productId,
      clientAdminId: userInfo?.userId,
    });
    setSearchTerm('');
    setStatusFilter('ALL');
    setProductFilter(defaultProductFilter);
  };
  return (
    <div className="content-space-y-6">
      {/* Header */}
      <div className="content-space-y-3">
        {fromProductPage && (
          <IconBackButton
            to={routes.productList.path}
            label="Back to Product List"
          />
        )}
        <div className="content-flex content-items-center content-justify-between">
          <div>
            <h1 className="content-text-3xl content-font-bold content-text-white dark:content-text-white">
              Sub Packages
            </h1>
            <p className="content-mt-2 content-text-gray-600 dark:content-text-gray-400">
              Manage and organize training content packages for your organization
            </p>
          </div>
          {userInfo.onboardBy !== 'TRIAL' && (
            <Button
              onClick={() => setIsOpenModal(modalTypes.CreateSubPackage)}
              variant="default"
            >
              <Plus className="content-size-4" /> Create Sub-Package
            </Button>
          )}
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-justify-between">
            <div className="content-flex content-items-center content-gap-2">
              <Filter className="content-size-5" />
              Filters & Search
            </div>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="content-flex content-flex-row content-items-center content-justify-between content-gap-2">
            <div className="content-relative content-w-1/2">
              <Search className="content-absolute content-left-3 content-top-1/2 content-size-4 -content-translate-y-1/2 content-transform content-text-gray-400" />
              <Input
                placeholder="Search sub-packages by name..."
                value={searchTerm}
                onChange={e => {
                  setSearchTerm(e.target.value);
                  setQueryParams({
                    ...queryParams,
                    search: e.target.value,
                  });
                }}
                className="content-pl-10"
              />
            </div>
            <div className="content-flex content-w-[45%] content-flex-row content-gap-2">
              <Select
                value={statusFilter}
                onValueChange={value => {
                  setStatusFilter(value);
                  setQueryParams({
                    ...queryParams,
                    status:
                      value === 'ALL'
                        ? CourseStatus.ALL
                        : (value as CourseStatus),
                  });
                }}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Filter by Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Status</SelectItem>
                  <SelectItem value="ACTIVE">Active</SelectItem>
                  <SelectItem value="INACTIVE">Inactive</SelectItem>
                  <SelectItem value="EXPIRED">Expired</SelectItem>
                </SelectContent>
              </Select>
              <Select
                value={productFilter}
                onValueChange={value => {
                  setProductFilter(value);
                  setQueryParams({
                    ...queryParams,
                    productId: value === 'all' ? '' : value,
                  });
                }}
                disabled={uniqueProducts.length === 0}
              >
                <SelectTrigger>
                  <SelectValue
                    placeholder={
                      uniqueProducts.length === 0
                        ? 'No products available'
                        : 'Filter by Product'
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  {uniqueProducts.length > 1 && (
                    <SelectItem value="all">All Products</SelectItem>
                  )}
                  {uniqueProducts.map(product => (
                    <SelectItem
                      key={product.productId}
                      value={product.productId}
                    >
                      {product.product?.productName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <Button
              className="content-w-1/6"
              variant="outline"
              onClick={handleReset}
            >
              Reset
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
          {loading ? (
            <AssignedPackagesLoader />
          ) : (
            <div className="content-overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>SL</TableHead>
                    <TableHead>Sub-Package Name</TableHead>
                    <TableHead>Product Name</TableHead>
                    <TableHead className="content-text-center">
                      Topics
                    </TableHead>
                    <TableHead className="content-text-center">
                      Assigned Users
                    </TableHead>
                    <TableHead className="content-text-center">
                      Created Date
                    </TableHead>
                    <TableHead className="content-text-center">
                      Status
                    </TableHead>
                    <TableHead className="content-text-center">
                      Last Updated
                    </TableHead>
                    <TableHead className="content-text-center">
                      Actions
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {subPackages?.items?.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={10} className="content-text-center">
                        No sub-packages found
                      </TableCell>
                    </TableRow>
                  )}
                  {subPackages?.items?.length > 0 &&
                    subPackages?.items?.map((pkg, index) => (
                      <TableRow key={pkg.id}>
                        <TableCell>{index + 1}</TableCell>
                        <TableCell>{pkg.name}</TableCell>
                        <TableCell>{pkg.productName}</TableCell>
                        <TableCell className="content-text-center">
                          <Badge variant="secondary">
                            {pkg.topicId.length} topics
                          </Badge>
                        </TableCell>
                        <TableCell className="content-text-center">
                          <Badge variant="secondary">
                            {pkg.assignedUserCount}
                          </Badge>
                        </TableCell>
                        <TableCell className="content-text-center">
                          {formatDateAndTime(pkg.createdAt)}
                        </TableCell>
                        <TableCell className="content-text-center">
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
                        <TableCell className="content-text-center content-text-gray-700 dark:content-text-gray-300">
                          {formatDateAndTime(pkg.updatedAt)}
                        </TableCell>
                        <TableCell className="content-text-right">
                          <div className="content-flex content-items-center content-justify-end content-gap-2">
                            <span
                              title={
                                pkg.status === Status.INACTIVE
                                  ? 'This sub-package is inactive. Activate it before assigning it to users.'
                                  : 'Assign this sub-package to users.'
                              }
                            >
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

                            {/* <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleDuplicate(pkg.id, pkg.name)}
                          title="Duplicate Sub-Package"
                        >
                          <Copy className="content-h-3 content-w-3" />
                        </Button> */}
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
                                disabled={pkg.isAlreadyAssigned === true}
                                onClick={() => handleEdit(pkg)}
                              >
                                <Edit className="content-size-3" />
                              </Button>
                            </span>
                            {/* <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleDelete(pkg.id, pkg.name)}
                          title="Delete Sub-Package"
                        >
                          <Trash2 className="content-h-3 content-w-3" />
                        </Button> */}
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                </TableBody>
              </Table>
            </div>
          )}
          <div className="content-flex content-justify-end content-pt-4">
            <Pagination
              total={subPackages.total}
              perPage={subPackages.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>

      {isOpenModal === modalTypes.AssignSubPackage && (
        <AssignSubPackage
          refetch={fetchSubPackages}
          isOpen={isOpenModal === modalTypes.AssignSubPackage}
          onClose={() => setIsOpenModal(modalTypes.None)}
          selectedSubPackage={selectedSubPackage}
        />
      )}
      {isOpenModal === modalTypes.ViewSubPackage && (
        <ViewSubPackage
          selectedSubPackage={selectedSubPackage}
          isOpen={isOpenModal === modalTypes.ViewSubPackage}
          onClose={() => setIsOpenModal(modalTypes.None)}
        />
      )}
      {isOpenModal === modalTypes.EditSubPackage && (
        <EditSubPackage
          isOpen={isOpenModal === modalTypes.EditSubPackage}
          onClose={() => setIsOpenModal(modalTypes.None)}
          selectedSubPackage={selectedSubPackage}
          refetch={fetchSubPackages}
        />
      )}
      {isOpenModal === modalTypes.CreateSubPackage && (
        <CreateSubPackage
          isOpen={isOpenModal === modalTypes.CreateSubPackage}
          onClose={() => setIsOpenModal(modalTypes.None)}
          reLoad={fetchSubPackages}
        />
      )}
      {isOpenModal === modalTypes.ViewLearners && (
        <ViewLearners
          isOpen={isOpenModal === modalTypes.ViewLearners}
          onClose={() => setIsOpenModal(modalTypes.None)}
          selectedSubPackage={selectedSubPackage}
        />
      )}
    </div>
  );
};

export default ClientAdminSubPackages;
