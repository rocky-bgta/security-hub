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
import Border from 'components/UserBorder';
import ProductListSkeleton from 'components/skeleton/ProductList';
import PackageDetailsModal from 'features/package/client-admin/PackageDetailsModal';
import ContactSalesModal from 'features/product/msp-admin/ContactSalesModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { BookOpen, Eye, Filter, Search, ShoppingCart } from 'lucide-react';
import { IGetListParams, IList, IResponse, Status } from 'models/Global';
import { IAssignedLicense } from 'models/License';
import { IMspCatalogPackage, IMspCatalogProduct } from 'models/MspCatalog';
import { IProduct } from 'models/Product';
import { Fragment, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';
import { humanizeText, objectToQueryString } from 'utils/Helper';

enum ModalType {
  None = 'none',
  PackageDetails = 'packageDetails',
  ContactSales = 'contactSales',
}

const mapToAssignedLicense = (
  product: IMspCatalogProduct,
  pkg: IMspCatalogPackage,
): IAssignedLicense => ({
  id: pkg.mspProductId!,
  clientAdminId: null,
  productId: product.productId,
  packageId: pkg.packageId,
  product: {
    productId: product.productId,
    productName: product.productName,
    productDescription: product.productDescription,
    productStatus: Status.ENABLED,
    thumbnailUrl: product.thumbnailUrl,
    createdAt: '',
    updatedAt: '',
    lastModifiedBy: '',
    packages: [],
    tags: [],
  } as IProduct,
  packageDetails: {
    id: pkg.packageId,
    packageName: pkg.packageName,
    packageStatus: pkg.packageStatus,
  },
  licenseCount: 0,
  usedLicenseCount: 0,
  assignedAt: '',
  expiryDate: null,
  topicCount: 0,
  licenseStatus: pkg.licenseStatus ?? undefined,
});

const formatPrice = (price: number) =>
  new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
  }).format(price);

const MSPAdminProductList = () => {
  const navigate = useNavigate();
  const { userInfo } = useStore();

  const [selectedProduct, setSelectedProduct] =
    useState<IMspCatalogProduct | null>(null);
  const [selectedPackage, setSelectedPackage] = useState<IAssignedLicense>();
  const [isDialogOpen, setIsDialogOpen] = useState<ModalType>(ModalType.None);
  const [loading, setLoading] = useState<boolean>(true);

  const apiClient = useAPI();

  const [productData, setProductData] = useState<IList<IMspCatalogProduct>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 10,
  });
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    offset: 0,
    pageSize: 10,
    mspProductStatus: '',
  });
  const queryString = useMemo(
    () => objectToQueryString(queryParams),
    [queryParams],
  );
  const debouncedQueryString = useDebounce(queryString, 1000);

  useEffect(() => {
    if (!userInfo?.userId || !debouncedQueryString) return;

    const fetchProductData = async () => {
      setLoading(true);
      try {
        const response: IResponse<IList<IMspCatalogProduct>> =
          await apiClient.get(
            `${API_END_POINTS.MSP_ALL_PRODUCT_LIST.replace(
              ':mspId',
              userInfo.userId,
            )}?${debouncedQueryString}`,
          );
        setProductData(response.data);
      } catch (error) {
        console.error('Error fetching product catalog:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProductData();
  }, [apiClient, debouncedQueryString, userInfo?.userId]);

  const sortedProducts = useMemo(() => {
    return [...productData.items].sort((a, b) => {
      if (a.mspProductStatus !== b.mspProductStatus) {
        return a.mspProductStatus === 'ENABLED' ? -1 : 1;
      }
      return (a.displayOrder ?? 0) - (b.displayOrder ?? 0);
    });
  }, [productData.items]);

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleViewPackageDetails = (
    product: IMspCatalogProduct,
    pkg: IMspCatalogPackage,
  ) => {
    setSelectedPackage(mapToAssignedLicense(product, pkg));
    setIsDialogOpen(ModalType.PackageDetails);
  };

  const handleBuyNow = (product: IMspCatalogProduct) => {
    navigate(routes.buyProduct.path, {
      state: {
        preselectedProduct: {
          productId: product.productId,
          productName: product.productName,
          productDescription: product.productDescription,
        },
      },
    });
  };

  const handleContactSales = (product: IMspCatalogProduct) => {
    setSelectedProduct(product);
    setIsDialogOpen(ModalType.ContactSales);
  };

  const getStatusBadgeVariant = (status: string) => {
    if (status === 'ENABLED' || status === 'ACTIVE') return 'default';
    return 'destructive';
  };

  return (
    <div className="content-space-y-6 content-text-white">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-tracking-tight content-text-white">
            Product Management
          </h1>
          <p className="content-text-muted-foreground">
            Manage your organization&apos;s products
          </p>
        </div>
      </div>

      <Border>
        <CardHeader>
          <div className="content-flex content-flex-col content-gap-4 lg:content-flex-row lg:content-justify-between">
            <div>
              <CardTitle className="content-flex content-items-center content-gap-2">
                <BookOpen className="content-size-5" />
                Total Available Products ({productData.items.filter(item => item.mspProductStatus === 'ENABLED').length})
              </CardTitle>
              <CardDescription>
                Browse and assign products to users
              </CardDescription>
            </div>
            <div className="content-flex content-w-full content-flex-col content-gap-2 sm:content-flex-row lg:content-w-auto">
              <div className="content-relative content-flex-1 lg:content-w-80">
                <Search className="content-absolute content-left-3 content-top-3 content-size-4 content-text-muted-foreground" />
                <Input
                  id="searchTerm"
                  placeholder="Search products..."
                  value={queryParams.search}
                  onChange={e => {
                    setQueryParams({
                      ...queryParams,
                      search: e.target.value,
                      offset: 0,
                    });
                  }}
                  className="content-w-full content-bg-transparent content-pl-9"
                />
              </div>
              <div className="content-w-full sm:content-w-64">
                <Select
                  value={queryParams.mspProductStatus || 'all'}
                  onValueChange={value => {
                    setQueryParams(prevState =>
                      value === 'all'
                        ? {
                            ...prevState,
                            mspProductStatus: '',
                            offset: 0,
                          }
                        : {
                            ...prevState,
                            mspProductStatus: value,
                            offset: 0,
                          },
                    );
                  }}
                >
                  <SelectTrigger className="content-bg-transparent">
                    <Filter className="content-mr-2 content-size-4" />
                    <SelectValue placeholder="Filter by MSP assignment status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Status</SelectItem>
                    <SelectItem value="ENABLED">Enabled</SelectItem>
                    <SelectItem value="DISABLED">Disabled</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <ProductListSkeleton />
          ) : (
            <Fragment>
              <CardContent className="!content-p-0">
                <div className="content-space-y-6">
                  {productData.items.length === 0 && (
                    <div className="content-flex content-h-full content-items-center content-justify-center">
                      <p className="content-text-sm content-text-gray-500">
                        No products found
                      </p>
                    </div>
                  )}

                  {sortedProducts.map(product => {
                    const isEnabled = product.mspProductStatus === 'ENABLED';

                    return (
                      <Card key={product.productId}>
                        <CardHeader>
                          <div className="content-flex content-flex-col content-gap-4 sm:content-flex-row sm:content-items-start sm:content-justify-between">
                            <div className="content-flex-1">
                              <div className="content-mb-2 content-flex content-flex-wrap content-items-center content-gap-3">
                                <CardTitle className="content-text-xl">
                                  {product.productName || 'Untitled Product'}
                                </CardTitle>
                                <Badge
                                  variant={getStatusBadgeVariant(
                                    product.mspProductStatus,
                                  )}
                                >
                                  {humanizeText(product.mspProductStatus)}
                                </Badge>
                                {isEnabled && product.licenseStatus && (
                                  <Badge
                                    variant={getStatusBadgeVariant(
                                      product.licenseStatus,
                                    )}
                                  >
                                    {humanizeText(product.licenseStatus)}
                                  </Badge>
                                )}
                              </div>
                              <CardDescription className="content-mt-2">
                                {product.productDescription ||
                                  'No description provided'}
                              </CardDescription>
                            </div>

                            {!isEnabled && (
                              <div className="content-flex content-flex-col content-gap-2 sm:content-flex-row">
                                <Button
                                  size="sm"
                                  onClick={() => handleBuyNow(product)}
                                  className="content-gap-2"
                                >
                                  <ShoppingCart className="content-size-4" />
                                  Buy Now
                                </Button>
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() => handleContactSales(product)}
                                  className="content-bg-transparent"
                                >
                                  Contact Sales Team
                                </Button>
                              </div>
                            )}
                          </div>
                        </CardHeader>
                        <CardContent>
                          <div className="content-space-y-4">
                            <div>
                              <h4 className="content-mb-3 content-font-medium content-text-ash-gray">
                                Packages
                              </h4>

                              <div className="content-overflow-x-auto">
                                <Table>
                                  <TableHeader>
                                    <TableRow>
                                      <TableHead>Package Name</TableHead>
                                      {isEnabled ? (
                                        <>
                                          <TableHead className="content-text-center">
                                            Package Status
                                          </TableHead>
                                          <TableHead className="content-text-center">
                                            License Status
                                          </TableHead>
                                          <TableHead className="content-text-center">
                                            Action
                                          </TableHead>
                                        </>
                                      ) : (
                                        <>
                                          <TableHead className="content-text-center">
                                            Price
                                          </TableHead>
                                          <TableHead className="content-text-center">
                                            Status
                                          </TableHead>
                                        </>
                                      )}
                                    </TableRow>
                                  </TableHeader>
                                  <TableBody>
                                    {product.packages.length === 0 ? (
                                      <TableRow>
                                        <TableCell
                                          colSpan={isEnabled ? 4 : 3}
                                          className="content-text-center content-text-muted-foreground"
                                        >
                                          No packages available
                                        </TableCell>
                                      </TableRow>
                                    ) : (
                                      product.packages.map(pkg => (
                                        <TableRow key={pkg.packageId}>
                                          <TableCell>
                                            {pkg.packageName}
                                          </TableCell>
                                          {isEnabled ? (
                                            <>
                                              <TableCell className="content-text-center">
                                                <Badge
                                                  variant={getStatusBadgeVariant(
                                                    pkg.packageStatus,
                                                  )}
                                                >
                                                  {humanizeText(
                                                    pkg.packageStatus,
                                                  )}
                                                </Badge>
                                              </TableCell>
                                              <TableCell className="content-text-center">
                                                {pkg.mspProductId &&
                                                pkg.licenseStatus ? (
                                                  <Badge
                                                    variant={getStatusBadgeVariant(
                                                      pkg.licenseStatus,
                                                    )}
                                                  >
                                                    {humanizeText(
                                                      pkg.licenseStatus,
                                                    )}
                                                  </Badge>
                                                ) : (
                                                  <span className="content-text-sm content-text-muted-foreground">
                                                    Not Assigned
                                                  </span>
                                                )}
                                              </TableCell>
                                              <TableCell className="content-flex content-justify-center content-gap-2">
                                                {pkg.mspProductId && (
                                                  <Button
                                                    title="View Details"
                                                    variant="ghost"
                                                    size="icon"
                                                    onClick={() =>
                                                      handleViewPackageDetails(
                                                        product,
                                                        pkg,
                                                      )
                                                    }
                                                  >
                                                    <Eye className="content-size-5" />
                                                  </Button>
                                                )}
                                              </TableCell>
                                            </>
                                          ) : (
                                            <>
                                              <TableCell className="content-text-center">
                                                {formatPrice(pkg.price)}
                                              </TableCell>
                                              <TableCell className="content-text-center">
                                                <Badge
                                                  variant={getStatusBadgeVariant(
                                                    pkg.licenseStatus ?? '',
                                                  )}
                                                >
                                                  {pkg.licenseStatus ===
                                                  'ACTIVE'
                                                    ? humanizeText(
                                                        pkg.licenseStatus,
                                                      )
                                                    : 'Not Assigned'}
                                                </Badge>
                                              </TableCell>
                                            </>
                                          )}
                                        </TableRow>
                                      ))
                                    )}
                                  </TableBody>
                                </Table>
                              </div>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    );
                  })}
                </div>
              </CardContent>
            </Fragment>
          )}
          <div className="content-flex content-justify-end content-py-7">
            <Pagination
              total={productData.total}
              perPage={productData.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Border>

      {isDialogOpen === ModalType.PackageDetails && selectedPackage && (
        <PackageDetailsModal
          isOpen={isDialogOpen === ModalType.PackageDetails}
          onClose={() => setIsDialogOpen(ModalType.None)}
          packageData={selectedPackage}
        />
      )}

      {isDialogOpen === ModalType.ContactSales && selectedProduct && (
        <ContactSalesModal
          isOpen={isDialogOpen === ModalType.ContactSales}
          onClose={() => {
            setIsDialogOpen(ModalType.None);
            setSelectedProduct(null);
          }}
          product={selectedProduct}
        />
      )}
    </div>
  );
};

export default MSPAdminProductList;
