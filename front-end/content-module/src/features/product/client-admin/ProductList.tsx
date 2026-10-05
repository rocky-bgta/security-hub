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
import TopicsListModal from 'features/package/client-admin/TopicsListModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { BookOpen, Eye, List, PackagePlus, Search } from 'lucide-react';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { IAssignedLicense } from 'models/License';
import { Fragment, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';
import { HumanizeDate, objectToQueryString } from 'utils/Helper';
import CreateAndAssignProduct from 'features/package/sub-package/create-sub-package/CreateAndAssignProduct';
import { ProductPreviewModal } from 'features/product/client-admin/ProductPreviewModal';

enum ModalType {
  None = 'none',
  Preview = 'preview',
  UserAssign = 'userAssign',
  PackageDetails = 'packageDetails',
  TopicsList = 'topicsList',
}

const ClientAdminProductList = () => {
  const navigate = useNavigate();
  const { userInfo } = useStore();

  const [selectedProduct, setSelectedProduct] = useState<IAssignedLicense>();
  const [selectedPackage, setSelectedPackage] = useState<IAssignedLicense>();
  const [isDialogOpen, setIsDialogOpen] = useState<ModalType>(ModalType.None);
  const [loading, setLoading] = useState<boolean>(true);

  const apiClient = useAPI();

  const [productData, setProductData] = useState<IList<IAssignedLicense>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 10,
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    clientAdminId: userInfo?.userId,
    offset: 0,
    pageSize: 10,
  });
  const searchDebounce = useDebounce(queryString, 1000);

  useEffect(() => {
    setTimeout(() => {
      const resp = objectToQueryString(queryParams);
      setQueryString(resp);
    }, 0);
  }, [queryParams]);

  useEffect(() => {
    if (!userInfo?.userId) return;

    const fetchProductData = async () => {
      setLoading(true);
      const response: IResponse<IList<IAssignedLicense>> = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_ASSIGN_PRODUCT_LIST.replace(
          ':clientAdminId',
          userInfo?.userId,
        ) + queryString,
      );
      setProductData(response.data);
      setLoading(false);
    };

    if (searchDebounce) {
      fetchProductData();
    }
  }, [apiClient, searchDebounce, userInfo]);

  const handleCreateSubPackage = (product: IAssignedLicense) => {
    setSelectedProduct(product);
    setIsDialogOpen(ModalType.UserAssign);
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleViewPackageDetails = (pkg: IAssignedLicense) => {
    setIsDialogOpen(ModalType.PackageDetails);
    setSelectedPackage(pkg);
  };

  const handleViewTopicsList = (product: IAssignedLicense) => {
    setIsDialogOpen(ModalType.TopicsList);
    setSelectedProduct(product);
  };

  const handleViewSubPackageList = (product: IAssignedLicense) => {
    navigate(
      `${routes.subPackages.path}?productId=${encodeURIComponent(product.productId)}&from=product`,
    );
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
          <div className="content-flex content-justify-between">
            <div>
              <CardTitle className="content-flex content-items-center content-gap-2">
                <BookOpen className="content-size-5" />
                Total Available Products ({productData?.items?.length || 0})
              </CardTitle>
              <CardDescription>
                Browse and assign products to users
              </CardDescription>
            </div>
            <div className="content-flex content-gap-2">
              <div className="content-relative content-flex-1">
                <Search className="content-absolute content-left-3 content-top-3 content-size-4 content-text-muted-foreground" />
                <Input
                  id="searchTerm"
                  placeholder="Search products..."
                  value={queryParams.search}
                  onChange={e =>
                    setQueryParams({
                      ...queryParams,
                      search: e.target.value,
                    })
                  }
                  className="content-w-80 content-bg-transparent content-pl-9"
                />
              </div>
              {/* <Select value={categoryFilter} onValueChange={setCategoryFilter}>
              <SelectTrigger className="!content-w-64 content-bg-transparent">
                <Filter className="content-mr-2 content-h-4 content-w-4" />
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="security">Active</SelectItem>
                <SelectItem value="compliance">Inactive</SelectItem>
              </SelectContent>
            </Select> */}
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
                  {productData?.items?.length === 0 && (
                    <div className="content-flex content-h-full content-items-center content-justify-center">
                      <p className="content-text-sm content-text-gray-500">
                        No products found
                      </p>
                    </div>
                  )}

                  {productData?.items?.map((product, index) => (
                    <Card key={index}>
                      <CardHeader>
                        <div className="content-flex content-items-start content-justify-between">
                          <div className="content-flex-1">
                            <div className="content-mb-2 content-flex content-items-center content-gap-3">
                              <CardTitle className="content-text-xl">
                                {product?.product?.productName ||
                                  'Untitled Product'}
                              </CardTitle>
                            </div>
                            <CardDescription className="content-mt-2">
                              {product?.product?.productDescription ||
                                'No description provided'}
                            </CardDescription>
                          </div>
                        </div>
                      </CardHeader>
                      <CardContent>
                        <div className="content-space-y-4">
                          {/* Packages */}
                          <div>
                            <h4 className="content-mb-3 content-font-medium content-text-ash-gray">
                              Packages
                            </h4>

                            <Table>
                              <TableHeader>
                                <TableRow>
                                  <TableHead>Package Name</TableHead>
                                  <TableHead className="content-text-center">
                                    Total License
                                  </TableHead>
                                  <TableHead className="content-text-center">
                                    Assigned License
                                  </TableHead>
                                  <TableHead className="content-text-center">
                                    Available License
                                  </TableHead>
                                  <TableHead className="content-text-center">
                                    Total Topics
                                  </TableHead>
                                  <TableHead>Start Date</TableHead>
                                  <TableHead>Expire Date</TableHead>
                                  <TableHead className="content-flex content-items-center content-justify-center">
                                    Action
                                  </TableHead>
                                </TableRow>
                              </TableHeader>
                              <TableBody>
                                <TableRow>
                                  <TableCell>
                                    {product?.packageDetails?.packageName}
                                  </TableCell>
                                  <TableCell className="content-text-center">
                                    {product?.licenseCount}
                                  </TableCell>
                                  <TableCell className="content-text-center">
                                    {product?.usedLicenseCount}
                                  </TableCell>
                                  <TableCell className="content-text-center">
                                    {product?.licenseCount -
                                      product?.usedLicenseCount}
                                  </TableCell>
                                  <TableCell className="content-text-center">
                                    {product?.topicCount}
                                  </TableCell>
                                  <TableCell>
                                    {product?.assignedAt
                                      ? HumanizeDate(product?.assignedAt)
                                      : '-'}
                                  </TableCell>

                                  <TableCell>
                                    {product?.expiryDate
                                      ? HumanizeDate(product?.expiryDate)
                                      : '-'}
                                  </TableCell>

                                  <TableCell className="content-flex content-justify-center content-gap-2">
                                    {product?.product?.tags?.includes(
                                      'Security',
                                    ) && (
                                      <>
                                        <Button
                                          title="Create Sub Package"
                                          variant="default"
                                          size="icon"
                                          onClick={() =>
                                            handleCreateSubPackage(product)
                                          }
                                          disabled={product?.packageDetails?.packageName
                                            .toLowerCase()
                                            .includes('trial')}
                                        >
                                          <PackagePlus className="content-size-5" />
                                        </Button>
                                        <Button
                                          title="Sub Package List"
                                          variant="ghost"
                                          size="icon"
                                          onClick={() =>
                                            handleViewSubPackageList(product)
                                          }
                                        >
                                          <List className="content-size-5" />
                                        </Button>
                                      </>
                                    )}

                                    <Button
                                      title="View Details"
                                      variant="ghost"
                                      size="icon"
                                      onClick={() =>
                                        handleViewPackageDetails(product)
                                      }
                                    >
                                      <Eye className="content-size-5" />
                                    </Button>
                                    <Button
                                      title="View Topics List"
                                      variant="ghost"
                                      size="icon"
                                      onClick={() =>
                                        handleViewTopicsList(product)
                                      }
                                    >
                                      <BookOpen className="content-size-5" />
                                    </Button>
                                  </TableCell>
                                </TableRow>
                              </TableBody>
                            </Table>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              </CardContent>
            </Fragment>
          )}
          <div className="content-flex content-justify-end content-py-7">
            <Pagination
              total={productData?.total}
              perPage={productData?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Border>
      {isDialogOpen === ModalType.Preview && (
        <ProductPreviewModal
          product={selectedProduct as IAssignedLicense}
          isOpen={isDialogOpen === ModalType.Preview}
          onClose={() => setIsDialogOpen(ModalType.None)}
        />
      )}
      {isDialogOpen === ModalType.UserAssign && (
        <CreateAndAssignProduct
          isOpen={isDialogOpen === ModalType.UserAssign}
          product={selectedProduct as IAssignedLicense}
          onClose={() => setIsDialogOpen(ModalType.None)}
        />
      )}
      {isDialogOpen === ModalType.PackageDetails && selectedPackage && (
        <PackageDetailsModal
          isOpen={isDialogOpen === ModalType.PackageDetails}
          onClose={() => setIsDialogOpen(ModalType.None)}
          packageData={selectedPackage}
        />
      )}
      {isDialogOpen === ModalType.TopicsList && (
        <TopicsListModal
          isOpen={isDialogOpen === ModalType.TopicsList}
          onClose={() => setIsDialogOpen(ModalType.None)}
          data={selectedProduct as IAssignedLicense}
        />
      )}
    </div>
  );
};

export default ClientAdminProductList;
