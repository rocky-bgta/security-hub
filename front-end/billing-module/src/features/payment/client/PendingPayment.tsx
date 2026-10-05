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
import TableLoader from 'components/skeleton/TableLoader';
import InvoiceDetailsDialog from 'features/payment/PendingPaymentDetails';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { DollarSignIcon, Download, Eye, FileText } from 'lucide-react';
import { IGetListParams, IList, IResponse, Status } from 'models/Global';
import { IClientAssignedProduct } from 'models/License';
import { IPaymentReport } from 'models/Payment';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { formatDateTime, objectToQueryString } from 'utils/Helper';
import dayjs from 'dayjs';

interface dateRange {
  startDate: string;
  endDate: string;
}

const getStatusParam = (status?: Status) => {
  const currentStatus = status as string;
  if (!currentStatus || currentStatus === Status.ALL || currentStatus === '') {
    return `&status=${Status.PENDING}&status=${Status.ON_PROGRESS}&status=${Status.EXPIRED}`;
  }
  return '';
};

const getStatusBadge = (status: string) => {
  switch (status) {
    case Status.PAID:
      return <Badge variant="default">Paid</Badge>;
    case Status.PENDING:
      return <Badge variant="secondary">Pending</Badge>;
    case Status.CANCELLED:
      return <Badge variant="destructive">Failed</Badge>;
    case Status.EXPIRED:
      return <Badge variant="destructive">Expired</Badge>;
    case Status.ON_PROGRESS:
      return (
        <Badge
          variant="outline"
          className="border border-orange-500 bg-orange-500 text-white"
        >
          On Progress
        </Badge>
      );
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

const getUniqueProducts = (items: IClientAssignedProduct[]) =>
  items.filter(
    (product, index, self) =>
      product.product &&
      index === self.findIndex(p => p.productId === product.productId),
  );

const ClientPendingPayment = () => {
  const { userInfo } = useStore();
  const [selectedId, setSelectedId] = useState<string>('');
  const [selectedDate, setSelectedDate] = useState<dateRange>({
    startDate: '',
    endDate: '',
  });
  const [pendingInvoices, setPendingInvoices] = useState<IList<IPaymentReport>>(
    {
      ...InitGetListParams,
      total: 0,
      items: [],
    },
  );
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    startDate: '',
    endDate: '',
    status: '' as Status,
    productId: '',
    clientId: userInfo?.userId,
  });
  const [products, setProducts] = useState<IList<IClientAssignedProduct>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [productFilter, setProductFilter] = useState<string>('');
  const searchDebounce = useDebounce(queryString, 1000);
  const [loading, setLoading] = useState<boolean>(true);
  const [openDetailsModal, setOpenDetailsModal] = useState<boolean>(false);
  // const [summaryData, setSummaryData] = useState<ISummaryData>();
  const [downloadLoading, setDownloadLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchProductData();
    }
  }, [searchDebounce]);

  useEffect(() => {
    if (selectedDate.startDate && selectedDate.endDate) {
      setQueryParams(prevState => ({
        ...prevState,
        startDate: selectedDate.startDate,
        endDate: selectedDate.endDate,
      }));
    }
  }, [selectedDate]);

  const uniqueProducts = getUniqueProducts(products.items);

  const fetchProducts = async () => {
    if (!userInfo?.userId) return;
    try {
      const response: IResponse<IList<IClientAssignedProduct>> =
        await apiClient.get(
          API_END_POINTS.CLIENT_ADMIN_PENDING_AND_ACTIVE_PRODUCT_LIST.replace(
            ':clientAdminId',
            userInfo.userId,
          ) + 'offset=0&pageSize=100',
        );
      setProducts(response.data);
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [userInfo?.userId]);

  useEffect(() => {
    const unique = getUniqueProducts(products.items);
    if (unique.length === 0) {
      setProductFilter('');
      return;
    }
    if (unique.length === 1) {
      const productId = unique[0].productId;
      setProductFilter(productId);
      setQueryParams(prev => ({ ...prev, productId }));
      return;
    }
    setProductFilter(prev =>
      prev && unique.some(p => p.productId === prev) ? prev : 'all',
    );
  }, [products.items]);

  const fetchProductData = async () => {
    setLoading(true);

    try {
      const statusParam = getStatusParam(queryParams.status);
      const response = await apiClient.get(
        API_END_POINTS.INVOICE_LIST + queryString + statusParam,
      );
      setPendingInvoices({
        ...InitGetListParams,
        total: response?.data?.total,
        items: response?.data?.items,
      });
    } catch (error) {
      console.error('Error fetching pending invoices:', error);
    } finally {
      setLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    setSelectedDate({
      startDate: '',
      endDate: '',
    });
    const defaultProductId =
      uniqueProducts.length === 1 ? uniqueProducts[0].productId : '';
    const defaultProductFilter =
      uniqueProducts.length === 1 ? uniqueProducts[0].productId : 'all';
    setProductFilter(defaultProductFilter);
    setQueryParams({
      ...InitGetListParams,
      status: '' as Status,
      search: '',
      startDate: '',
      endDate: '',
      productId: defaultProductId,
      clientId: userInfo?.userId,
    });
  };

  const handleExportPendingPayment = async () => {
    setDownloadLoading(true);

    try {
      const statusParam = getStatusParam(queryParams.status);
      const response = await apiClient.get(
        API_END_POINTS.PENDING_PAYMENT_EXPORT_CSV + queryString + statusParam,
        {
          responseType: 'blob',
        },
      );

      const blob = new Blob([response], {
        type: 'text/csv;charset=utf-8;',
      });

      const url = window.URL.createObjectURL(blob);

      const a = document.createElement('a');
      a.href = url;
      a.download = `payment_history_${dayjs().format('DD-MM-YYYY')}.csv`;
      document.body.appendChild(a);
      a.click();

      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading payment history:', error);
    } finally {
      setDownloadLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Pending List for Payment
          </h1>
          <p className="text-white text-opacity-75">
            View and manage pending payment invoices
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2 text-white">
                <FileText className="size-5" />
                Pending Payment List
              </CardTitle>
              <CardDescription className="text-gray-400">
                Invoices awaiting payment from your organization
              </CardDescription>
            </div>
            {loading ? (
              <div className="h-8 w-24 animate-pulse rounded bg-card-border"></div>
            ) : (
              pendingInvoices?.total > 0 && (
                <Button onClick={handleExportPendingPayment} variant="outline">
                  <Download className="mr-2 size-4" />
                  {downloadLoading ? 'Exporting...' : 'Export invoices'}
                </Button>
              )
            )}
          </div>
        </CardHeader>
        <CardContent>
          <div className="mb-6 grid grid-cols-7 gap-2">
            <div className="col-span-2 flex flex-col items-start gap-2">
              <Label htmlFor="search">Search</Label>
              <Input
                className="w-full"
                id="search"
                type="text"
                placeholder="Search by Invoice No."
                value={queryParams.search || ''}
                onChange={e =>
                  setQueryParams(prev => ({
                    ...prev,
                    search: e.target.value,
                  }))
                }
              />
            </div>
            <div className="col-span-1 flex flex-col items-start gap-2">
              <Label htmlFor="status">Status</Label>
              <Select
                value={queryParams.status || Status.ALL}
                onValueChange={value =>
                  setQueryParams(prev => ({
                    ...prev,
                    status:
                      value === Status.ALL ? ('' as Status) : (value as Status),
                  }))
                }
              >
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="Select Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={Status.ALL}>All</SelectItem>
                  <SelectItem value={Status.PENDING}>Pending</SelectItem>
                  <SelectItem value={Status.ON_PROGRESS}>
                    On Progress
                  </SelectItem>
                  <SelectItem value={Status.EXPIRED}>Expired</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="col-span-1 flex flex-col items-start gap-2">
              <Label htmlFor="product">Product</Label>
              <Select
                value={productFilter || undefined}
                onValueChange={value => {
                  setProductFilter(value);
                  setQueryParams(prev => ({
                    ...prev,
                    productId: value === 'all' ? '' : value,
                    offset: 0,
                  }));
                }}
                disabled={uniqueProducts.length === 0}
              >
                <SelectTrigger className="w-full">
                  <SelectValue
                    placeholder={
                      uniqueProducts.length === 0
                        ? 'No products available'
                        : 'Select Product'
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
            <div className="col-span-1 flex flex-col items-start gap-2">
              <Label htmlFor="startDate">Start Date</Label>
              <Input
                className="w-full"
                id="startDate"
                type="date"
                value={selectedDate.startDate}
                onChange={e =>
                  setSelectedDate(prev => ({
                    ...prev,
                    startDate: e.target.value,
                  }))
                }
                onClick={e => {
                  const input = e.target as HTMLInputElement;
                  input.showPicker();
                }}
                max={new Date().toISOString().split('T')[0]}
              />
            </div>
            <div className="col-span-1 flex flex-col items-start gap-2">
              <Label htmlFor="endDate">End Date</Label>
              <Input
                className="w-full"
                id="endDate"
                type="date"
                value={selectedDate.endDate}
                onChange={e =>
                  setSelectedDate(prev => ({
                    ...prev,
                    endDate: e.target.value,
                  }))
                }
                onClick={e => {
                  const input = e.target as HTMLInputElement;
                  input.showPicker();
                }}
                min={selectedDate.startDate}
                max={new Date().toISOString().split('T')[0]}
              />
            </div>
            <div className="col-span-1 flex items-end justify-end">
              <Button
                variant="outline"
                className="w-full"
                onClick={handleReset}
              >
                Reset
              </Button>
            </div>
          </div>
          {loading ? (
            <TableLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Invoice No.</TableHead>
                  <TableHead>Product Name</TableHead>
                  <TableHead>Package Name</TableHead>
                  <TableHead className="text-center">
                    Sub Total Amount
                  </TableHead>
                  <TableHead className="text-center">Coupon Amount</TableHead>
                  <TableHead className="text-center">Discount Amount</TableHead>
                  <TableHead className="text-center">Vat Amount</TableHead>
                  <TableHead className="text-center">Total Amount</TableHead>
                  <TableHead>Created Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {pendingInvoices?.items?.length > 0 ? (
                  pendingInvoices?.items?.map(invoice => (
                    <TableRow key={invoice.id}>
                      <TableCell className="font-medium">
                        {invoice.id}
                      </TableCell>
                      <TableCell>
                        {invoice.productSelections.map((product, index) => (
                          <span key={index}>
                            {invoice.productSelections.length > 1 &&
                              `${index + 1}. `}
                            {product.productName}
                            {index < invoice.productSelections.length - 1 && (
                              <br />
                            )}
                          </span>
                        ))}
                      </TableCell>
                      <TableCell>
                        {invoice.productSelections.map((product, index) => (
                          <span key={index}>
                            {invoice.productSelections.length > 1 &&
                              `${index + 1}. `}
                            {product.packageName}
                            {index < invoice.productSelections.length - 1 && (
                              <br />
                            )}
                          </span>
                        ))}
                      </TableCell>
                      <TableCell className="text-center">
                        ${invoice.subtotal.toFixed(2)}
                      </TableCell>
                      <TableCell className="text-center">
                        ${invoice.couponDiscountAmount.toFixed(2) || 0}
                      </TableCell>
                      <TableCell className="text-center">
                        ${invoice.discountAmount.toFixed(2)}
                      </TableCell>
                      <TableCell className="text-center">
                        ${invoice.vatAmount.toFixed(2)}
                      </TableCell>
                      <TableCell className="text-center">
                        ${invoice.totalAmount.toFixed(2)}
                      </TableCell>
                      <TableCell>{formatDateTime(invoice.createdAt)}</TableCell>
                      <TableCell>{getStatusBadge(invoice.status)}</TableCell>
                      <TableCell>
                        <Button
                          size="sm"
                          variant={
                            invoice.status === Status.EXPIRED ||
                            invoice.paymentMethod !== null
                              ? 'outline'
                              : 'default'
                          }
                          onClick={() => {
                            setSelectedId(invoice.id);
                            setOpenDetailsModal(true);
                          }}
                          title={
                            invoice.status === Status.EXPIRED ||
                            invoice.paymentMethod !== null
                              ? 'View Details'
                              : 'Pay Now'
                          }
                        >
                          {invoice.status !== Status.EXPIRED &&
                          invoice.paymentMethod === null ? (
                            <span className="flex items-center gap-1">
                              <DollarSignIcon className="mr-1 size-3" />
                              Pay Now
                            </span>
                          ) : (
                            <span className="flex items-center gap-1">
                              <Eye className="mr-1 size-3" />
                              View Details
                            </span>
                          )}
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={10} className="text-center">
                      <div className="text-center text-muted-foreground">
                        No pending payments found.
                      </div>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          )}
          <div className="pt-6">
            <Pagination
              total={pendingInvoices?.total || 0}
              perPage={pendingInvoices?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>

      {openDetailsModal === true && (
        <InvoiceDetailsDialog
          paymentId={selectedId}
          open={openDetailsModal}
          onClose={() => setOpenDetailsModal(false)}
        />
      )}
    </div>
  );
};

export default ClientPendingPayment;
