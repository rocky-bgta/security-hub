import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from 'common/Dropdown';
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
import DeleteConfirmationModal from 'components/DeleteConfirmationModal';
import TableLoader from 'components/skeleton/TableLoader';
import CouponModal from 'features/coupon/CouponModal';
import QRCodeModal from 'features/coupon/QRCodeModal';
import ViewCouponModal from 'features/coupon/ViewCouponModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import {
  Calendar,
  Edit,
  Eye,
  FileDown,
  Filter,
  Loader2,
  Search,
  ShieldAlert,
  Ticket,
  Trash,
  Users,
} from 'lucide-react';
import { ICoupon, ICouponSummary } from 'models/Coupon';
import {
  IDropdownOption,
  IGetListParams,
  IList,
  ModalType,
  OrderType,
} from 'models/Global';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  formatDateTime,
  getUsagePercentage,
  isExpired,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

const CouponList = () => {
  const [data, setData] = useState<IList<ICoupon>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    pageSize: 10,
    searchParam: '',
    isActive: '',
    isExpired: '',
    couponType: '',
    productId: '',
    order: OrderType.DESC,
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const [loading, setLoading] = useState<boolean>(true);
  const [isShowModal, setIsShowModal] = useState<ModalType>(ModalType.NONE);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
  const [isQRModalOpen, setIsQRModalOpen] = useState<boolean>(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [selectedCoupon, setSelectedCoupon] = useState<ICoupon | null>(null);
  const [productList, setProductList] = useState<IDropdownOption[]>([]);
  const [couponSummary, setCouponSummary] = useState<ICouponSummary>({
    totalCoupons: 0,
    activeCoupons: 0,
    inactiveCoupons: 0,
    totalUsage: 0,
    estimatedSavings: 0,
  });
  const apiClient = useAPI();
  const [exportLoading, setExportLoading] = useState(false);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchCouponList();
    }
  }, [searchDebounce]);

  const fetchCouponList = async () => {
    setLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.COUPON_LIST + queryString,
      );
      setData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching coupon list:', error);
      toast.error('Failed to fetch coupon list');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProductList();
    fetchCouponSummary();
  }, []);

  const fetchProductList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.ENABLED_PRODUCT_LIST + 'pageSize=1000',
      );

      if (isSuccessResponse(response.statusCode)) {
        setProductList(
          response.data.items.map((item: any) => ({
            label: item.productName,
            value: item.productId,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching product list:', error);
      toast.error('Failed to fetch product list');
    }
  };

  const fetchCouponSummary = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.COUPON_SUMMARY);
      if (isSuccessResponse(response.statusCode)) {
        setCouponSummary(response.data);
      }
    } catch (error) {
      console.error('Error fetching coupon summary:', error);
      toast.error('Failed to fetch coupon summary');
    }
  };
  const getStatusBadge = (coupon: string) => {
    if (coupon === 'INACTIVE') {
      return <Badge variant="secondary">Inactive</Badge>;
    }
    if (coupon === 'EXPIRED') {
      return <Badge variant="destructive">Expired</Badge>;
    }
    return <Badge className="bg-green-500 hover:bg-green-600">Active</Badge>;
  };

  const handleEditCoupon = (coupon: ICoupon) => {
    setSelectedCoupon(coupon);
    setIsShowModal(ModalType.EDIT);
  };

  const handleViewCoupon = (coupon: ICoupon) => {
    setSelectedCoupon(coupon);
    setIsViewModalOpen(true);
  };

  const handleDeleteCoupon = async () => {
    setDeleteLoading(true);

    try {
      await apiClient.del(API_END_POINTS.COUPON_DELETE + selectedCoupon?.id);
      toast.success('Coupon deleted successfully');
      setIsDeleteModalOpen(false);
      fetchCouponList();
      fetchCouponSummary();
    } catch (error) {
      console.error('Error deleting coupon:', error);
      toast.error('Failed to delete coupon');
    } finally {
      setDeleteLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleCloseModal = () => {
    setIsShowModal(ModalType.NONE);
    setSelectedCoupon(null);
  };

  const handleResetFilters = () => {
    setQueryParams({
      ...InitGetListParams,
      searchParam: '',
      productId: '',
      couponType: '',
      isActive: '',
      isExpired: '',
    });
  };

  const handleExportCSV = async () => {
    try {
      setExportLoading(true);
      const response = await apiClient.get(API_END_POINTS.COUPON_EXPORT_CSV);

      const blob = new Blob([response], { type: 'text/csv' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'coupons.csv';
      a.click();
    } catch (error) {
      console.error('Error exporting CSV:', error);
      toast.error('Failed to export CSV');
    } finally {
      setExportLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Coupon Management</h1>
          <p className="text-muted-foreground">
            Manage your promotional coupons and discount codes
          </p>
        </div>
        <Button onClick={() => setIsShowModal(ModalType.ADD)}>
          <Ticket className="mr-2 size-4" />
          Add Coupon
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Coupons</CardTitle>
            <Ticket className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {couponSummary.totalCoupons}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active</CardTitle>
            <Calendar className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {couponSummary.activeCoupons}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Used</CardTitle>
            <Users className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{couponSummary.totalUsage}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Expired</CardTitle>
            <ShieldAlert className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {data?.items?.filter(coupon => isExpired(coupon.validUntil)).length || 0}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Filters</CardTitle>
            <div>
              <Button
                onClick={() => handleExportCSV()}
                variant="outline"
                disabled={exportLoading}
              >
                <FileDown className="size-4" />
                Export CSV
                {exportLoading && <Loader2 className="size-4 animate-spin" />}
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-7">
            <div className="relative col-span-3">
              <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
              <Input
                placeholder="Search coupons..."
                value={queryParams.searchParam}
                onChange={e =>
                  setQueryParams({
                    ...queryParams,
                    searchParam: e.target.value,
                  })
                }
                className="pl-10"
              />
            </div>
            <div>
              <Select
                value={queryParams.productId}
                onValueChange={value =>
                  setQueryParams({
                    ...queryParams,
                    productId: value,
                  })
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a product" />
                </SelectTrigger>
                <SelectContent>
                  {productList.map(item => (
                    <SelectItem key={item.value} value={item.value}>
                      {item.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Select
                value={queryParams.couponType}
                onValueChange={value =>
                  setQueryParams({
                    ...queryParams,
                    couponType: value === 'ALL' ? '' : value,
                  })
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a coupon type" />
                </SelectTrigger>

                <SelectContent>
                  <SelectItem value="ALL">All</SelectItem>
                  <SelectItem value="PERCENTAGE">Percentage</SelectItem>
                  <SelectItem value="FIXED">Fixed</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline">
                  <Filter className="mr-2 size-4" />
                  Status:{' '}
                  {queryParams.isActive === '' && queryParams.isExpired === ''
                    ? 'All'
                    : queryParams.isActive
                      ? 'Active'
                      : queryParams.isExpired
                        ? 'Expired'
                        : 'Inactive'}
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent>
                <DropdownMenuItem
                  onClick={() =>
                    setQueryParams({
                      ...queryParams,
                      isActive: '',
                      isExpired: '',
                    })
                  }
                >
                  All
                </DropdownMenuItem>
                <DropdownMenuItem
                  onClick={() =>
                    setQueryParams({
                      ...queryParams,
                      isActive: true,
                      isExpired: '',
                    })
                  }
                >
                  Active
                </DropdownMenuItem>
                <DropdownMenuItem
                  onClick={() =>
                    setQueryParams({
                      ...queryParams,
                      isActive: false,
                      isExpired: '',
                    })
                  }
                >
                  Inactive
                </DropdownMenuItem>
                <DropdownMenuItem
                  onClick={() =>
                    setQueryParams({
                      ...queryParams,
                      isExpired: true,
                      isActive: '',
                    })
                  }
                >
                  Expired
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <Button variant="outline" onClick={() => handleResetFilters()}>
              Reset
            </Button>
          </div>
        </CardContent>
      </Card>
      {/* Table */}
      {loading ? (
        <TableLoader />
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Code</TableHead>
              <TableHead>Type</TableHead>
              <TableHead className="text-center">Value</TableHead>
              <TableHead className="text-center">Usage Limit</TableHead>
              <TableHead className="text-center">Min Purchase Amount</TableHead>
              <TableHead>Valid Until</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-center">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {data?.items?.length === 0 && (
              <TableRow>
                <TableCell colSpan={9} className="text-center">
                  No coupons found
                </TableCell>
              </TableRow>
            )}
            {data?.items?.map(coupon => (
              <TableRow key={coupon.id}>
                <TableCell>
                  <div>
                    <div className="font-medium">{coupon.name}</div>
                  </div>
                </TableCell>
                <TableCell>
                  <code className="rounded bg-muted px-2 py-1 text-sm">
                    {coupon.code}
                  </code>
                </TableCell>
                <TableCell>
                  <Badge variant="outline">{coupon.type}</Badge>
                </TableCell>
                <TableCell className="text-center">
                  {coupon.type === 'PERCENTAGE'
                    ? `${coupon.value}%`
                    : `$${coupon.value}`}
                </TableCell>
                <TableCell>
                  <div className="flex items-center justify-center gap-2">
                    <span className="text-sm">
                      {coupon.totalUsed}/{coupon.usageLimit}
                    </span>

                    <div className="h-2 w-full rounded-full bg-white/35">
                      <div
                        className="h-2 rounded-full bg-primary"
                        style={{
                          width: `${getUsagePercentage(coupon.totalUsed, coupon.usageLimit)}%`,
                        }}
                      />
                    </div>
                  </div>
                </TableCell>
                <TableCell className="text-center">
                  {coupon.minPurchaseAmount
                    ? `$${coupon.minPurchaseAmount}`
                    : 'N/A'}
                </TableCell>
                <TableCell>{formatDateTime(coupon.validUntil)}</TableCell>
                <TableCell>
                  {getStatusBadge(coupon.active ? (isExpired(coupon.validUntil) ? 'EXPIRED' : 'ACTIVE') : 'INACTIVE')}
                </TableCell>
                <TableCell className="text-center">
                  <div className="flex items-center justify-center gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleViewCoupon(coupon)}
                    >
                      <Eye className="size-4" />
                    </Button>
                    {/* <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => {
                          setSelectedCoupon(coupon);
                          setIsQRModalOpen(true);
                        }}
                      >
                        <QrCode className="h-4 w-4" />
                      </Button> */}
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleEditCoupon(coupon)}
                    >
                      <Edit className="size-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => {
                        setIsDeleteModalOpen(true);
                        setSelectedCoupon(coupon);
                      }}
                    >
                      <Trash className="size-4" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
      <div className="flex justify-end py-7">
        <Pagination
          total={data?.total || 0}
          perPage={data?.pageSize}
          onPageChange={onPageChangeHandler}
        />
      </div>

      {/* Modals */}

      {isShowModal === ModalType.ADD && (
        <CouponModal
          isOpen={isShowModal === ModalType.ADD}
          onClose={handleCloseModal}
          onSave={() => {
            fetchCouponList();
            fetchCouponSummary();
          }}
        />
      )}

      {isShowModal === ModalType.EDIT && (
        <CouponModal
          isOpen={isShowModal === ModalType.EDIT}
          onClose={handleCloseModal}
          onSave={() => {
            fetchCouponList();
            fetchCouponSummary();
          }}
          coupon={selectedCoupon as ICoupon | null}
        />
      )}

      <DeleteConfirmationModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteCoupon}
        title="Delete Coupon"
        loading={deleteLoading}
      />

      <ViewCouponModal
        isOpen={isViewModalOpen}
        onClose={() => {
          setIsViewModalOpen(false);
          setSelectedCoupon(null);
        }}
        coupon={selectedCoupon}
      />

      {isQRModalOpen && (
        <QRCodeModal
          isOpen={isQRModalOpen}
          onClose={() => {
            setIsQRModalOpen(false);
            setSelectedCoupon(null);
          }}
          qrCodeUrl={selectedCoupon?.qrCodeUrl || ''}
          couponCode={selectedCoupon?.code || ''}
          onQRCodeGenerated={() => {
            fetchCouponList();
            fetchCouponSummary();
          }}
        />
      )}
    </div>
  );
};

export default CouponList;
