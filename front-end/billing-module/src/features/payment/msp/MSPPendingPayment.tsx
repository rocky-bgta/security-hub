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
import { IGetListParams, IList, Status } from 'models/Global';
import { IPaymentReport } from 'models/Payment';
import { useCallback, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { formatDateTime, objectToQueryString } from 'utils/Helper';
import dayjs from 'dayjs';
import { ROLE } from 'utils/Role';

interface dateRange {
  startDate: string;
  endDate: string;
}

const getStatusParam = (currentQueryString: string) => {
  const currentStatus = new URLSearchParams(currentQueryString).get('status');
  if (!currentStatus || currentStatus === Status.ALL || currentStatus === '') {
    return `&status=${Status.PENDING}&status=${Status.ON_PROGRESS}`;
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

const formatAmount = (amount?: number | null) => `$${(amount ?? 0).toFixed(2)}`;

const getUniqueProductSelections = (
  productSelections: Array<IPaymentReport['productSelections'][number]>,
) =>
  productSelections.filter(
    (product, index, self) =>
      index === self.findIndex(item => item.productId === product.productId),
  );

const MSPPendingPayment = () => {
  const apiClient = useAPI();
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
    mspId: userInfo?.userId || '',
    roleType: ROLE.MSP_ADMIN,
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const [loading, setLoading] = useState<boolean>(true);
  const [openDetailsModal, setOpenDetailsModal] = useState<boolean>(false);
  const [downloadLoading, setDownloadLoading] = useState<boolean>(false);

  const fetchProductData = useCallback(
    async (currentQueryString: string) => {
      setLoading(true);

      try {
        const statusParam = getStatusParam(currentQueryString);
        const response = await apiClient.get(
          API_END_POINTS.INVOICE_LIST + currentQueryString + statusParam,
        );
        setPendingInvoices({
          ...InitGetListParams,
          total: response?.data?.total ?? 0,
          items: response?.data?.items ?? [],
        });
      } catch (error) {
        console.error('Error fetching pending invoices:', error);
      } finally {
        setLoading(false);
      }
    },
    [apiClient],
  );

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (userInfo?.userId && searchDebounce) {
      fetchProductData(searchDebounce);
    }
  }, [fetchProductData, searchDebounce, userInfo?.userId]);

  useEffect(() => {
    if (!userInfo?.userId) return;

    setQueryParams(prevState =>
      prevState.mspId === userInfo.userId
        ? prevState
        : {
            ...prevState,
            mspId: userInfo.userId,
          },
    );
  }, [userInfo?.userId]);

  useEffect(() => {
    if (selectedDate.startDate && selectedDate.endDate) {
      setQueryParams(prevState => ({
        ...prevState,
        startDate: selectedDate.startDate,
        endDate: selectedDate.endDate,
      }));
    }
  }, [selectedDate]);

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
    setQueryParams({
      ...InitGetListParams,
      status: '' as Status,
      search: '',
      startDate: '',
      endDate: '',
      mspId: userInfo?.userId || '',
    });
  };

  const handleExportPendingPayment = async () => {
    setDownloadLoading(true);

    try {
      const statusParam = getStatusParam(queryString);
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
                MSP Pending Payment List
              </CardTitle>
              <CardDescription className="text-gray-400">
                Invoices awaiting payment from your organization
              </CardDescription>
            </div>
            {loading ? (
              <div className="h-8 w-24 animate-pulse rounded bg-card-border" />
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
          <div className="mb-6 grid grid-cols-6 gap-2">
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
                        {getUniqueProductSelections(
                          invoice.productSelections || [],
                        ).map((product, index, products) => (
                          <span key={product.productId}>
                            {products.length > 1 && `${index + 1}. `}
                            {product.productName}
                            {index < products.length - 1 && <br />}
                          </span>
                        ))}
                      </TableCell>
                      <TableCell>
                        {(invoice.productSelections || []).map(
                          (product, index) => (
                            <span key={`${product.packageId}-${index}`}>
                              {invoice.productSelections.length > 1 &&
                                `${index + 1}. `}
                              {product.packageName}
                              {index < invoice.productSelections.length - 1 && (
                                <br />
                              )}
                            </span>
                          ),
                        )}
                      </TableCell>
                      <TableCell className="text-center">
                        {formatAmount(invoice.subtotal)}
                      </TableCell>
                      <TableCell className="text-center">
                        {formatAmount(invoice.couponDiscountAmount)}
                      </TableCell>
                      <TableCell className="text-center">
                        {formatAmount(invoice.discountAmount)}
                      </TableCell>
                      <TableCell className="text-center">
                        {formatAmount(invoice.vatAmount)}
                      </TableCell>
                      <TableCell className="text-center">
                        {formatAmount(invoice.totalAmount)}
                      </TableCell>
                      <TableCell>{formatDateTime(invoice.createdAt)}</TableCell>
                      <TableCell>{getStatusBadge(invoice.status)}</TableCell>
                      <TableCell>
                        <Button
                          size="sm"
                          variant={
                            !invoice.paymentMethod ? 'default' : 'outline'
                          }
                          onClick={() => {
                            setSelectedId(invoice.id);
                            setOpenDetailsModal(true);
                          }}
                          title={
                            !invoice.paymentMethod ? 'Pay Now' : 'View Details'
                          }
                        >
                          {!invoice.paymentMethod ? (
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
                    <TableCell colSpan={12} className="text-center">
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

export default MSPPendingPayment;
