import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from 'common/DropdownMenu';
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
import ProductListSkeleton from 'components/skeleton/ProductList';
import NewModal from 'features/product/NewModal';
import ProductViewModal from 'features/product/ProductViewModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import {
  ChevronDown,
  Edit,
  Eye,
  Filter,
  Package,
  PackageCheck,
  Plus,
  Search,
} from 'lucide-react';
import {
  CourseStatus,
  IGetListParams,
  IList,
  IResponse,
  Status,
} from 'models/Global';
import { IProduct } from 'models/Product';
import { Fragment, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';
import MspAssignProduct from './assign-product/AssignProduct';

enum ModalType {
  General = 'general',
  View = 'view',
  Assign = 'assign',
  None = 'none',
}

const SuperAdminProductList = () => {
  const [showModal, setShowModal] = useState<ModalType>(ModalType.None);
  const [data, setData] = useState<IList<IProduct>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    status: CourseStatus.ALL,
  });
  const [productIdName, setProductIdName] = useState({
    id: '',
    name: '',
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [showPackage, setShowPackage] = useState<boolean>(false);
  const searchDebounce = useDebounce(queryString, 1000);

  const apiClient = useAPI();

  const products = data.items;
  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchProductData();
    }
  }, [searchDebounce]);

  const fetchProductData = async () => {
    setLoading(true);

    try {
      const response: IResponse<IList<IProduct>> = await apiClient.get(
        API_END_POINTS.PRODUCT_LIST + queryString,
      );
      setData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching product data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateNew = () => {
    setShowModal(ModalType.General);
  };

  const handleHideModal = () => {
    setShowModal(ModalType.None);
    if (productIdName) setProductIdName({ id: '', name: '' });
    setShowPackage(false);
  };

  const handleSubmit = () => fetchProductData();

  const handleViewProduct = (id: string) => {
    setShowModal(ModalType.View);
    if (id) setProductIdName({ id, name: '' });
  };

  const handleEditProduct = (id: string) => {
    setShowModal(ModalType.General);
    if (id) setProductIdName({ id, name: '' });
  };

  const handleAddPackage = (id: string) => {
    setShowModal(ModalType.General);
    setShowPackage(true);
    if (id) setProductIdName({ id, name: '' });
  };

  const handleStatusChange = async (
    productIndex: number,
    newStatus: Status,
    product: IProduct,
  ) => {
    const payload = {
      productName: product.productName,
      productDescription: product.productDescription,
      productStatus: newStatus,
      packages: product.packages,
    };

    try {
      const response: IResponse<IProduct> = await apiClient.put(
        API_END_POINTS.PRODUCT_UPDATE + product.productId,
        { data: payload },
      );

      if ([200, 201].includes(response.statusCode)) {
        toast.success(response.message);
        const updatedProducts = [...products];

        updatedProducts[productIndex].productStatus = newStatus;
        setData({
          ...data,
          items: updatedProducts,
        });
      } else {
        toast.error(response.message);
      }
    } catch (error) {
      console.error('Error updating product status:', error);
    }
  };

  const getStatusColor = (status: Status) => {
    switch (status) {
      case Status.ENABLED:
        return '!content-bg-primary content-text-white';
      case Status.DISABLED:
        return '!content-bg-vibrant-red content-text-white';
      default:
        return '!content-bg-gray-100 !content-text-gray-800';
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <div>
      <div>
        {/* Header */}
        <div className="content-mb-8 content-flex content-items-center content-justify-between">
          <div className="content-flex content-items-center content-gap-3">
            <Package className="content-size-8 content-text-primary" />
            <div>
              <h1 className="content-text-3xl content-font-bold content-text-white">
                Product Management
              </h1>
              <p className="content-text-cloudy-white">
                Manage your training products and packages
              </p>
            </div>
          </div>
          <div className="content-flex content-items-center content-gap-2">
            <div className="content-relative">
              <Button onClick={() => setShowModal(ModalType.Assign)}>
                <PackageCheck />
                Assign Product
              </Button>
            </div>
            <Button onClick={handleCreateNew}>
              <Plus className="content-mr-2 content-size-4" />
              Create New Product
            </Button>
          </div>
        </div>

        {/* Search and Filter Controls */}
        <Card className="content-mb-6 content-border-0">
          <CardContent className="!content-p-6">
            <div className="content-flex content-flex-col content-gap-4 md:content-flex-row">
              <div className="content-flex-1">
                <div className="content-relative">
                  <Search className="content-absolute content-left-3 content-top-1/2 content-size-4 -content-translate-y-1/2 content-transform content-text-gray-400" />
                  <Input
                    id="search"
                    placeholder="Search products, descriptions, or topics..."
                    value={queryParams.search}
                    onChange={e => {
                      setQueryParams(prevState => ({
                        ...prevState,
                        search: e.target.value,
                        offset: 0,
                      }));
                    }}
                    className="content-px-3 content-py-2 content-pl-10"
                  />
                </div>
              </div>
              <div className="content-w-full md:content-w-48">
                <Select
                  value={queryParams.status}
                  onValueChange={value => {
                    setQueryParams(prevState =>
                      value === 'all'
                        ? {
                          ...prevState,
                          status: '' as CourseStatus,
                          offset: 0,
                        }
                        : {
                          ...prevState,
                          status: value as CourseStatus,
                          offset: 0,
                        },
                    );
                  }}
                >
                  <SelectTrigger>
                    <Filter className="content-mr-2 content-size-4" />
                    <SelectValue placeholder="Filter by status" />
                  </SelectTrigger>
                  <SelectContent className="content-z-50">
                    <SelectItem value="all">All Status</SelectItem>
                    {/* <SelectItem value="draft">Draft</SelectItem>
                    <SelectItem value="published">Published</SelectItem> */}
                    <SelectItem value={CourseStatus.ENABLED}>
                      Enabled
                    </SelectItem>
                    <SelectItem value={CourseStatus.DISABLED}>
                      Disabled
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Products List */}
        <Card>
          {loading ? (
            <ProductListSkeleton />
          ) : (
            <Fragment>
              <CardHeader>
                <CardTitle className="content-text-2xl content-text-white">
                  Products
                </CardTitle>
                <CardDescription>
                  {products.length === 0
                    ? "No products created yet. Click 'Create New Product' to get started."
                    : `${data.total} product${data.total === 1 ? '' : 's'} found`}
                </CardDescription>
              </CardHeader>
              <CardContent>
                {products.length === 0 ? (
                  <div className="content-py-12 content-text-center">
                    <Package className="content-mx-auto content-mb-4 content-size-16 content-text-gray-300" />
                    <h3 className="content-mb-2 content-text-lg content-font-medium content-text-ash-gray">
                      No products yet
                    </h3>
                    <p className="content-mb-6 content-text-gray-500">
                      Create your first product to get started with package
                      management.
                    </p>
                    <Button
                      onClick={handleCreateNew}
                      className="content-bg-blue-600 content-text-white hover:content-bg-blue-700"
                    >
                      <Plus className="content-mr-2 content-size-4" />
                      Create Your First Product
                    </Button>
                  </div>
                ) : (
                  <div className="content-space-y-6">
                    {products.map((product) => {
                      const originalIndex = products.findIndex(
                        p => p.productId === product.productId,
                      );
                      return (
                        <Card key={originalIndex}>
                          <CardHeader>
                            <div className="content-flex content-items-start content-justify-between">
                              <div className="content-flex-1">
                                <div className="content-mb-2 content-flex content-items-center content-gap-3">
                                  <CardTitle className="content-text-xl content-text-white">
                                    {product.productName || 'Untitled Product'}
                                  </CardTitle>
                                  <DropdownMenu>
                                    <DropdownMenuTrigger asChild>
                                      <Button
                                        variant="outline"
                                        size="sm"
                                        className="content-h-8"
                                      >
                                        <Badge
                                          className={`content-mr-2 ${getStatusColor(product.productStatus)}`}
                                        >
                                          {product.productStatus || 'N/A'}
                                        </Badge>
                                        <ChevronDown className="content-size-4" />
                                      </Button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent
                                      className="content-z-50 content-border content-bg-white content-shadow-lg"
                                      align="start"
                                    >
                                      <DropdownMenuItem
                                        onClick={() =>
                                          handleStatusChange(
                                            originalIndex,
                                            Status.ENABLED,
                                            product,
                                          )
                                        }
                                        className="content-cursor-pointer"
                                      >
                                        <Badge className="!content-bg-primary content-text-white">
                                          Enabled
                                        </Badge>
                                        Enabled
                                      </DropdownMenuItem>
                                      <DropdownMenuItem
                                        onClick={() =>
                                          handleStatusChange(
                                            originalIndex,
                                            Status.DISABLED,
                                            product,
                                          )
                                        }
                                        className="content-cursor-pointer"
                                      >
                                        <Badge className="!content-bg-vibrant-red content-text-white">
                                          Disabled
                                        </Badge>
                                        Disabled
                                      </DropdownMenuItem>
                                    </DropdownMenuContent>
                                  </DropdownMenu>
                                </div>
                                <CardDescription className="content-mt-2">
                                  {product.productDescription ||
                                    'No description provided'}
                                </CardDescription>
                              </div>
                              <div className="content-flex content-gap-2">
                                <Button
                                  variant="outline"
                                  size="sm"
                                  onClick={() =>
                                    handleViewProduct(product.productId)
                                  }
                                >
                                  <Eye className="content-mr-1 content-size-4" />
                                  View
                                </Button>
                                <Button
                                  variant="outline"
                                  size="sm"
                                  onClick={() =>
                                    handleEditProduct(product.productId)
                                  }
                                >
                                  <Edit className="content-mr-1 content-size-4" />
                                  Edit
                                </Button>
                                <Button
                                  size="sm"
                                  onClick={() =>
                                    handleAddPackage(product.productId)
                                  }
                                  className="content-bg-green-600 content-text-white hover:content-bg-green-700"
                                >
                                  <Plus className="content-mr-1 content-size-4" />
                                  Add Package
                                </Button>
                              </div>
                            </div>
                          </CardHeader>
                          <CardContent>
                            <div className="content-space-y-4">
                              {/* Packages */}
                              <div>
                                <h4 className="content-mb-3 content-font-medium content-text-ash-gray">
                                  Packages ({product?.packages?.length})
                                </h4>
                                {product?.packages?.length > 0 ? (
                                  <Table>
                                    <TableHeader>
                                      <TableRow>
                                        <TableHead>Package Name</TableHead>
                                        <TableHead>Price</TableHead>
                                        {/* <TableHead>Features</TableHead> */}
                                        {/* <TableHead>Topics</TableHead> */}
                                        <TableHead>Status</TableHead>
                                      </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                      {product?.packages?.map(
                                        (pkg, pkgIndex) => (
                                          <TableRow key={pkgIndex}>
                                            <TableCell className="content-font-medium">
                                              {pkg.packageName}
                                            </TableCell>
                                            <TableCell>
                                              {pkg.price === 0
                                                ? 'Free'
                                                : `$${pkg.price}`}
                                            </TableCell>
                                            {/* <TableCell>
                                        <div className="content-flex content-flex-wrap content-gap-1">
                                          {pkg.featureIds
                                            .slice(0, 2)
                                            .map(
                                              (
                                                feature: string,
                                                featureIndex: number,
                                              ) => (
                                                <Badge
                                                  key={featureIndex}
                                                  variant="outline"
                                                  className="content-text-xs"
                                                >
                                                  {feature}
                                                </Badge>
                                              ),
                                            )}
                                          {pkg.featureIds.length > 2 && (
                                            <Badge
                                              variant="outline"
                                              className="content-text-xs"
                                            >
                                              +{pkg.featureIds.length - 2} more
                                            </Badge>
                                          )}
                                        </div>
                                      </TableCell> */}
                                            {/* <TableCell>
                                        <Badge
                                          variant="secondary"
                                          className="content-text-xs"
                                        >
                                          {pkg.assignedTopics.length} topics
                                        </Badge>
                                      </TableCell> */}
                                            <TableCell>
                                              <Badge
                                                className={`content-text-xs ${getStatusColor(pkg.packageStatus)}`}
                                              >
                                                {pkg.packageStatus}
                                              </Badge>
                                            </TableCell>
                                          </TableRow>
                                        ),
                                      )}
                                    </TableBody>
                                  </Table>
                                ) : (
                                  <p className="content-text-sm content-text-gray-500">
                                    No packages created yet
                                  </p>
                                )}
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      );
                    })}
                  </div>
                )}
              </CardContent>

            </Fragment>
          )}

          <div className="content-flex content-justify-end content-py-7">
            <Pagination
              total={data?.total}
              perPage={data?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </Card>
      </div>
      <NewModal
        productId={productIdName.id}
        isOpen={showModal === ModalType.General}
        onClose={handleHideModal}
        onSubmit={handleSubmit}
        showPackage={showPackage}
      />

      <ProductViewModal
        productId={productIdName.id}
        isOpen={showModal === ModalType.View}
        onClose={handleHideModal}
      />

      <MspAssignProduct
        isOpen={showModal === ModalType.Assign}
        onClose={handleHideModal}
      />
    </div>
  );
};

export default SuperAdminProductList;
