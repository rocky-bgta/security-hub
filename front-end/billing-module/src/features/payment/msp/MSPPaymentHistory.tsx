import dayjs from 'dayjs';
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
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { Download, Eye, History } from 'lucide-react';
import { IGetListParams, IList, Status } from 'models/Global';
import { IPaymentReport, ISummaryData } from 'models/Payment';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { humanizeText, objectToQueryString } from 'utils/Helper';
import {
  Select,
  SelectValue,
  SelectTrigger,
  SelectContent,
  SelectItem,
} from 'common/Select';
import TableLoader from 'components/skeleton/TableLoader';
import { useStore } from 'hooks/UseStore';
import ViewPaymentHistory from '../client/ViewPaymentHistory';
import { getStatusBadge } from '../MSPPaymentHistory';

interface dateRange {
  startDate: string;
  endDate: string;
}

const MSPAdminMSPPaymentHistory = () => {
  const { userInfo } = useStore();
  const [selectedDate, setSelectedDate] = useState<dateRange>({
    startDate: '',
    endDate: '',
  });
  const [selectedReportId, setSelectedReportId] = useState<string>();
  const [data, setData] = useState<IList<IPaymentReport>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    startDate: '',
    endDate: '',
    status: '' as Status,
    mspId: userInfo?.userId,
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [downloadLoading, setDownloadLoading] = useState<boolean>(false);
  const [openDetailsModal, setOpenDetailsModal] = useState<boolean>(false);
  const [summaryData, setSummaryData] = useState<ISummaryData>();

  const searchDebounce = useDebounce(queryString, 1000);

  const apiClient = useAPI();

  useEffect(() => {
    const fetchSummaryData = async () => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.SUMMARY_DATA + '?mspId=' + userInfo?.userId,
        );
        setSummaryData(response.data);
      } catch (error) {
        console.error('Error fetching summary data:', error);
      }
    };

    fetchSummaryData();
  }, [apiClient, userInfo?.userId]);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    const fetchPaymentReportData = async () => {
      setLoading(true);

      try {
        const response = await apiClient.get(
          API_END_POINTS.PAYMENT_HISTORY + queryString,
        );
        setData({
          ...InitGetListParams,
          total: response.data.total,
          items: response.data.items,
        });
      } catch (error) {
        console.error('Error fetching payment history:', error);
      } finally {
        setLoading(false);
      }
    };

    if (searchDebounce) {
      fetchPaymentReportData();
    }
  }, [apiClient, queryString, searchDebounce]);

  useEffect(() => {
    if (selectedDate.startDate && selectedDate.endDate) {
      setQueryParams(prevState => ({
        ...prevState,
        startDate: selectedDate.startDate,
        endDate: selectedDate.endDate,
      }));
    }
  }, [selectedDate]);

  const handleExportHistory = async () => {
    setDownloadLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.PAYMENT_HISTORY_EXPORT_CSV +
        'mspId=' +
        userInfo?.userId,
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
      a.download = `msp_payment_history_${dayjs().format('DD-MM-YYYY')}.csv`;
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

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    setQueryParams({
      ...InitGetListParams,
      status: '' as Status,
      startDate: '',
      endDate: '',
      mspId: userInfo?.userId,
    });
    setSelectedDate({
      startDate: '',
      endDate: '',
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">
            MSP Payment History
          </h1>
          <p className="text-base text-white text-opacity-75">
            Manage billing information and payment history
          </p>
        </div>
      </div>

      <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Total Payments
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-600">
              {summaryData?.totalPayments?.toFixed(2) || 0}
            </div>
            <p className="text-xs text-muted-foreground">Total payments</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Total Outstanding
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-600">
              {summaryData?.outstandingAmount?.toFixed(2) || 0}
            </div>
            <p className="text-xs text-muted-foreground">
              Total outstanding amount
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Paid Invoices</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {summaryData?.paidInvoicesCount?.toFixed(2) || 0}
            </div>
            <p className="text-xs text-muted-foreground">Paid invoices</p>
          </CardContent>
        </Card>
        {/* <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Not Yet Paid Invoices
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {summaryData?.overdueInvoicesCount || 0}
            </div>
            <p className="text-xs text-muted-foreground">
              Not yet paid invoices
            </p>
          </CardContent>
        </Card> */}
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <History className="size-5" />
                Payment History Overview
              </CardTitle>
              <CardDescription>
                View past payment records and invoices
              </CardDescription>
            </div>
            {loading ? (
              <div className="h-8 w-24 animate-pulse rounded bg-card-border"></div>
            ) : (
              data?.total > 0 && (
                <Button onClick={handleExportHistory} variant="outline">
                  <Download className="mr-2 size-4" />
                  {downloadLoading ? 'Exporting...' : 'Export History'}
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
                  <SelectItem value={Status.SUCCESS}>Success</SelectItem>
                  <SelectItem value={Status.PENDING}>Pending</SelectItem>
                  <SelectItem value={Status.CANCELLED}>Cancelled</SelectItem>
                  <SelectItem value={Status.FAILED}>Failed</SelectItem>
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
                // min={selectedDate.endDate}
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
                  <TableHead>Invoice Number</TableHead>
                  <TableHead className="text-center">
                    Sub Total Amount
                  </TableHead>
                  <TableHead className="text-center">Discount Amount</TableHead>
                  <TableHead className="text-center">Vat Amount</TableHead>
                  <TableHead className="text-center">Total Amount</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Payment Method</TableHead>
                  <TableHead>Date</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data?.items?.length > 0 ? (
                  <>
                    {data?.items?.map((payment, index) => (
                      <TableRow key={index}>
                        <TableCell className="font-medium">
                          {payment.invoiceId}
                        </TableCell>
                        <TableCell className="text-center">
                          ${payment.subtotal.toFixed(2)}
                        </TableCell>
                        <TableCell className="text-center">
                          ${payment.discountAmount.toFixed(2)}
                        </TableCell>
                        <TableCell className="text-center">
                          ${payment.vatAmount.toFixed(2)}
                        </TableCell>
                        <TableCell className="text-center">
                          ${payment.totalAmount.toFixed(2)}
                        </TableCell>
                        <TableCell>{getStatusBadge(payment.status)}</TableCell>
                        <TableCell>
                          {humanizeText(payment.paymentMethod)}
                        </TableCell>
                        <TableCell>
                          {payment?.date
                            ? dayjs(payment.date).format('DD-MM-YYYY')
                            : 'N/A'}
                        </TableCell>
                        <TableCell>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => {
                              setSelectedReportId(payment.id);
                              setOpenDetailsModal(true);
                            }}
                            title="View Details"
                          >
                            <Eye className="size-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </>
                ) : (
                  <TableRow>
                    <TableCell colSpan={9} className="p-4 text-center">
                      <div className="text-center text-muted-foreground">
                        No payment history available yet.
                      </div>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          )}
          <div className="pt-6">
            <Pagination
              total={data?.total}
              perPage={data?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>
      {openDetailsModal && (
        <ViewPaymentHistory
          id={selectedReportId || ''}
          onClose={() => setOpenDetailsModal(false)}
          isOpen={openDetailsModal}
        />
      )}
    </div>
  );
};

export default MSPAdminMSPPaymentHistory;
